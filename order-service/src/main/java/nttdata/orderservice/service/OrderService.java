package nttdata.orderservice.service;

import nttdata.orderservice.dto.request.CreateOrderRequest;
import nttdata.orderservice.dto.response.OrderResponse;
import nttdata.orderservice.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String username, CreateOrderRequest request);

    List<OrderResponse> getMyOrders(String username);

    OrderResponse getOrderById(Long orderId, String username, boolean isAdmin);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    OrderResponse cancelOrder(Long orderId, String username, boolean isAdmin);
}