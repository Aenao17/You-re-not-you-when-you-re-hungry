package nttdata.orderservice.dto.external;

import java.util.List;

public record RestaurantResponse(
        Long restaurantId,

        String name,

        String description,

        List<MenuItemResponse> menuItems
) {
}