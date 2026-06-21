package nttdata.orderservice.client;

import nttdata.orderservice.dto.external.MenuItemResponse;
import nttdata.orderservice.dto.external.RestaurantResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev")
public class FakeMenuClient implements MenuClient {

    @Override
    public RestaurantResponse getRestaurantById(Long restaurantId) {
        return new RestaurantResponse(
                restaurantId,
                "Demo Restaurant",
                "Temporary restaurant used for local Order Service development",
                List.of(
                        new MenuItemResponse(1L, "Pizza Margherita", BigDecimal.valueOf(35.50)),
                        new MenuItemResponse(2L, "Pasta Carbonara", BigDecimal.valueOf(42.00)),
                        new MenuItemResponse(3L, "Lemonade", BigDecimal.valueOf(12.00))
                )
        );
    }
}