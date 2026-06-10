package nttdata.orderservice.client;

import nttdata.orderservice.dto.external.RestaurantResponse;

public interface MenuClient {

    RestaurantResponse getRestaurantById(Long restaurantId);
}