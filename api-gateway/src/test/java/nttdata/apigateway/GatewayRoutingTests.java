package nttdata.apigateway;

import nttdata.apigateway.support.JwtTestTokens;
import nttdata.apigateway.support.WireMockServices;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTests {

    @LocalServerPort
    private int gatewayPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @DynamicPropertySource
    static void overrideServiceUrls(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:" + WireMockServices.USER.port());
        registry.add("MENU_SERVICE_URL", () -> "http://localhost:" + WireMockServices.MENU.port());
        registry.add("ORDER_SERVICE_URL", () -> "http://localhost:" + WireMockServices.ORDER.port());
    }

    @BeforeEach
    void resetStubs() {
        WireMockServices.resetAll();
    }

    private String gatewayUrl(String path) {
        return "http://localhost:" + gatewayPort + path;
    }

    private HttpEntity<Void> withAuth(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        return new HttpEntity<>(headers);
    }

    @Test
    void routesUserHelloToUserService() {
        WireMockServices.USER.stubFor(get(urlEqualTo("/api/users/hello"))
                .willReturn(aResponse().withStatus(200).withBody("hello-from-user")));

        ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl("/api/users/hello"),
                HttpMethod.GET,
                withAuth(JwtTestTokens.bearerCustomerToken()),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("hello-from-user");
    }

    @Test
    void routesAuthLoginToUserService() {
        WireMockServices.USER.stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse().withStatus(200).withBody("token-from-user")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                gatewayUrl("/api/auth/login"),
                new HttpEntity<>("{}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("token-from-user");
    }

    @Test
    void routesAdminUsersToUserService() {
        WireMockServices.USER.stubFor(get(urlEqualTo("/api/admin/users"))
                .willReturn(aResponse().withStatus(200).withBody("admin-users")));

        ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl("/api/admin/users"),
                HttpMethod.GET,
                withAuth(JwtTestTokens.bearerAdminToken()),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("admin-users");
    }

    @Test
    void routesMenuRestaurantsToMenuService() {
        WireMockServices.MENU.stubFor(get(urlEqualTo("/api/menu/restaurants"))
                .willReturn(aResponse().withStatus(200).withBody("restaurants")));

        ResponseEntity<String> response = restTemplate.getForEntity(gatewayUrl("/api/menu/restaurants"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("restaurants");
    }

    @Test
    void routesOrdersToOrderService() {
        WireMockServices.ORDER.stubFor(post(urlEqualTo("/api/orders"))
                .willReturn(aResponse().withStatus(201).withBody("order-created")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", JwtTestTokens.bearerCustomerToken());
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.postForEntity(
                gatewayUrl("/api/orders"),
                new HttpEntity<>("{}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("order-created");
    }

    @Test
    void forwardsAuthorizationHeaderToDownstreamService() {
        String token = JwtTestTokens.bearerCustomerToken();

        WireMockServices.ORDER.stubFor(get(urlEqualTo("/api/orders/my"))
                .withHeader("Authorization", equalTo(token))
                .willReturn(aResponse().withStatus(200).withBody("my-orders")));

        ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl("/api/orders/my"),
                HttpMethod.GET,
                withAuth(token),
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
