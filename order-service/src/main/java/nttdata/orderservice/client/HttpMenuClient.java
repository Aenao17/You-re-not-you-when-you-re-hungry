package nttdata.orderservice.client;

import lombok.RequiredArgsConstructor;
import nttdata.orderservice.dto.external.RestaurantResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Profile("!dev")
@RequiredArgsConstructor
public class HttpMenuClient implements MenuClient {

    private final RestClient menuRestClient;

    @Override
    public RestaurantResponse getRestaurantById(Long restaurantId) {
        try {
            return menuRestClient.get()
                    .uri("/api/menu/restaurants/{id}", restaurantId)
                    .retrieve()
                    .body(RestaurantResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return null;
            }
            throw ex;
        }
    }
}
