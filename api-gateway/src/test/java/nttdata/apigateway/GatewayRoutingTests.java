package nttdata.apigateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTests {

    private static final WireMockServer userService = new WireMockServer(wireMockConfig().dynamicPort());
    private static final WireMockServer menuService = new WireMockServer(wireMockConfig().dynamicPort());
    private static final WireMockServer orderService = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        userService.start();
        menuService.start();
        orderService.start();
    }

    @LocalServerPort
    private int gatewayPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @DynamicPropertySource
    static void overrideServiceUrls(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:" + userService.port());
        registry.add("MENU_SERVICE_URL", () -> "http://localhost:" + menuService.port());
        registry.add("ORDER_SERVICE_URL", () -> "http://localhost:" + orderService.port());
    }

    @AfterAll
    static void stopWireMock() {
        userService.stop();
        menuService.stop();
        orderService.stop();
    }

    @BeforeEach
    void resetStubs() {
        userService.resetAll();
        menuService.resetAll();
        orderService.resetAll();
    }

    private String gatewayUrl(String path) {
        return "http://localhost:" + gatewayPort + path;
    }

    @Test
    void routesUserHelloToUserService() {
        userService.stubFor(get(urlEqualTo("/api/users/hello"))
                .willReturn(aResponse().withStatus(200).withBody("hello-from-user")));

        ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl("/api/users/hello"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("hello-from-user");
    }

    @Test
    void routesAuthLoginToUserService() {
        userService.stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse().withStatus(200).withBody("token-from-user")));

        ResponseEntity<String> response = restTemplate.postForEntity(
                gatewayUrl("/api/auth/login"),
                "{}",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("token-from-user");
    }

    @Test
    void routesAdminUsersToUserService() {
        userService.stubFor(get(urlEqualTo("/api/admin/users"))
                .willReturn(aResponse().withStatus(200).withBody("admin-users")));

        ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl("/api/admin/users"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("admin-users");
    }

    @Test
    void routesMenuRestaurantsToMenuService() {
        menuService.stubFor(get(urlEqualTo("/api/menu/restaurants"))
                .willReturn(aResponse().withStatus(200).withBody("restaurants")));

        ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl("/api/menu/restaurants"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("restaurants");
    }

    @Test
    void routesOrdersToOrderService() {
        orderService.stubFor(post(urlEqualTo("/api/orders"))
                .willReturn(aResponse().withStatus(201).withBody("order-created")));

        ResponseEntity<String> response = restTemplate.postForEntity(
                gatewayUrl("/api/orders"),
                "{}",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("order-created");
    }

    @Test
    void forwardsAuthorizationHeaderToDownstreamService() {
        orderService.stubFor(get(urlEqualTo("/api/orders/my"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(aResponse().withStatus(200).withBody("my-orders")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer test-token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl("/api/orders/my"),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("my-orders");
    }

    @Test
    void returnsNotFoundForUnknownPath() {
        assertThatThrownBy(() -> restTemplate.getForEntity(gatewayUrl("/unknown"), String.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
