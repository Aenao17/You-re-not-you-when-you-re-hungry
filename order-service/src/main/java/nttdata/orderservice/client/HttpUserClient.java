package nttdata.orderservice.client;

import lombok.RequiredArgsConstructor;
import nttdata.orderservice.dto.external.UserResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Profile("!dev")
@RequiredArgsConstructor
public class HttpUserClient implements UserClient {

    private final RestClient userRestClient;

    @Override
    public UserResponse getUserByUsername(String username) {
        try {
            return userRestClient.get()
                    .uri("/api/internal/users/{username}", username)
                    .retrieve()
                    .body(UserResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return null;
            }
            throw ex;
        }
    }
}
