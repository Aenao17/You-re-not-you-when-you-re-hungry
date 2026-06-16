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
class GatewaySecurityTests {

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

    @Test
    void allowsPublicAuthRegisterWithoutToken() {
        WireMockServices.USER.stubFor(post(urlEqualTo("/api/auth/register"))
                .willReturn(aResponse().withStatus(201).withBody("registered")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                gatewayUrl("/api/auth/register"),
                new HttpEntity<>("{}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void allowsPublicMenuReadWithoutToken() {
        WireMockServices.MENU.stubFor(get(urlEqualTo("/api/menu/restaurants"))
                .willReturn(aResponse().withStatus(200).withBody("restaurants")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                gatewayUrl("/api/menu/restaurants"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void rejectsProtectedRouteWithoutToken() {
        assertThatThrownBy(() -> restTemplate.getForEntity(gatewayUrl("/api/orders/my"), String.class))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void rejectsProtectedRouteWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        assertThatThrownBy(() -> restTemplate.exchange(
                gatewayUrl("/api/users/hello"),
                HttpMethod.GET,
                entity,
                String.class
        ))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void allowsProtectedRouteWithValidToken() {
        WireMockServices.USER.stubFor(get(urlEqualTo("/api/users/hello"))
                .willReturn(aResponse().withStatus(200).withBody("hello-from-user")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", JwtTestTokens.bearerCustomerToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl("/api/users/hello"),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("hello-from-user");
    }

    @Test
    void forwardsAuthorizationHeaderToDownstreamService() {
        String token = JwtTestTokens.bearerCustomerToken();

        WireMockServices.ORDER.stubFor(get(urlEqualTo("/api/orders/my"))
                .withHeader("Authorization", equalTo(token))
                .willReturn(aResponse().withStatus(200).withBody("my-orders")));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
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
    void rejectsMenuMutationWithoutToken() {
        WireMockServices.MENU.stubFor(post(urlEqualTo("/api/menu/restaurants"))
                .willReturn(aResponse().withStatus(201).withBody("created")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        assertThatThrownBy(() -> restTemplate.postForEntity(
                gatewayUrl("/api/menu/restaurants"),
                new HttpEntity<>("{}", headers),
                String.class
        ))
                .isInstanceOf(RestClientResponseException.class)
                .satisfies(ex -> assertThat(((RestClientResponseException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
