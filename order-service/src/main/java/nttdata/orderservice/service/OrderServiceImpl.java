package nttdata.orderservice.service;

import lombok.RequiredArgsConstructor;
import nttdata.orderservice.client.MenuClient;
import nttdata.orderservice.dto.external.MenuItemResponse;
import nttdata.orderservice.dto.external.RestaurantResponse;
import nttdata.orderservice.dto.request.CreateOrderItemRequest;
import nttdata.orderservice.dto.request.CreateOrderRequest;
import nttdata.orderservice.dto.response.OrderItemResponse;
import nttdata.orderservice.dto.response.OrderResponse;
import nttdata.orderservice.entity.Order;
import nttdata.orderservice.entity.OrderItem;
import nttdata.orderservice.entity.OrderStatus;
import nttdata.orderservice.exception.BadRequestException;
import nttdata.orderservice.exception.ForbiddenException;
import nttdata.orderservice.exception.ResourceNotFoundException;
import nttdata.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuClient menuClient;

    @Override
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        RestaurantResponse restaurant = menuClient.getRestaurantById(request.getRestaurantId());

        if (restaurant == null) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId());
        }

        Order order = Order.builder()
                .username(username)
                .restaurantId(restaurant.restaurantId())
                .restaurantName(restaurant.name())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            MenuItemResponse menuItem = findMenuItemInRestaurant(
                    restaurant,
                    itemRequest.getMenuItemId()
            );

            BigDecimal subtotal = menuItem.price()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.id())
                    .menuItemName(menuItem.name())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(menuItem.price())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);

            totalPrice = totalPrice.add(subtotal);
        }

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String username) {
        return orderRepository.findByUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String username, boolean isAdmin) {
        Order order = findOrderOrThrow(orderId);

        if (!isAdmin && !order.getUsername().equals(username)) {
            throw new ForbiddenException("You are not allowed to view this order");
        }

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrderOrThrow(orderId);

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        return toResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String username, boolean isAdmin) {
        Order order = findOrderOrThrow(orderId);

        if (!isAdmin && !order.getUsername().equals(username)) {
            throw new ForbiddenException("You are not allowed to cancel this order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Completed orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        return toResponse(order);
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private MenuItemResponse findMenuItemInRestaurant(
            RestaurantResponse restaurant,
            Long menuItemId
    ) {
        if (restaurant.menuItems() == null || restaurant.menuItems().isEmpty()) {
            throw new BadRequestException("Restaurant has no menu items");
        }

        return restaurant.menuItems()
                .stream()
                .filter(item -> item.id().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Menu item " + menuItemId +
                                " does not belong to restaurant " + restaurant.restaurantId()
                ));
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new BadRequestException("New order status is required");
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be updated");
        }

        if (currentStatus == OrderStatus.COMPLETED) {
            throw new BadRequestException("Completed orders cannot be updated");
        }

        if (currentStatus == OrderStatus.CREATED && newStatus == OrderStatus.CONFIRMED) {
            return;
        }

        if (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.COMPLETED) {
            return;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            return;
        }

        throw new BadRequestException("Invalid order status transition from " + currentStatus + " to " + newStatus);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .username(order.getUsername())
                .restaurantId(order.getRestaurantId())
                .restaurantName(order.getRestaurantName())
                .items(itemResponses)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}