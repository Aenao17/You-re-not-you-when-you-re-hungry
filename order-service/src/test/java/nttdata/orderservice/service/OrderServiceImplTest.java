package nttdata.orderservice.service;

import nttdata.orderservice.client.MenuClient;
import nttdata.orderservice.dto.external.MenuItemResponse;
import nttdata.orderservice.dto.external.RestaurantResponse;
import nttdata.orderservice.dto.request.CreateOrderItemRequest;
import nttdata.orderservice.dto.request.CreateOrderRequest;
import nttdata.orderservice.dto.response.OrderResponse;
import nttdata.orderservice.entity.Order;
import nttdata.orderservice.entity.OrderItem;
import nttdata.orderservice.entity.OrderStatus;
import nttdata.orderservice.exception.BadRequestException;
import nttdata.orderservice.exception.ForbiddenException;
import nttdata.orderservice.exception.ResourceNotFoundException;
import nttdata.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuClient menuClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private RestaurantResponse restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantResponse(
                1L,
                "Demo Restaurant",
                "Test restaurant",
                List.of(
                        new MenuItemResponse(10L, "Pizza", BigDecimal.valueOf(35.50)),
                        new MenuItemResponse(11L, "Pasta", BigDecimal.valueOf(42.00))
                )
        );
    }

    @Test
    void createOrder_shouldCreateOrderAndCalculateTotal() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(1L)
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .menuItemId(10L)
                                .quantity(2)
                                .build(),
                        CreateOrderItemRequest.builder()
                                .menuItemId(11L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(menuClient.getRestaurantById(1L)).thenReturn(restaurant);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);

            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            long itemId = 1L;
            for (OrderItem item : order.getItems()) {
                item.setId(itemId++);
            }

            return order;
        });

        OrderResponse response = orderService.createOrder("customer@test.com", request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getUserEmail()).isEqualTo("customer@test.com");
        assertThat(response.getRestaurantId()).isEqualTo(1L);
        assertThat(response.getRestaurantName()).isEqualTo("Demo Restaurant");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getItems()).hasSize(2);

        assertThat(response.getTotalPrice()).isEqualByComparingTo("113.00");

        verify(menuClient).getRestaurantById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldSaveSnapshotOfMenuItemData() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(1L)
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .menuItemId(10L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(menuClient.getRestaurantById(1L)).thenReturn(restaurant);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder("customer@test.com", request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getUserEmail()).isEqualTo("customer@test.com");
        assertThat(savedOrder.getRestaurantId()).isEqualTo(1L);
        assertThat(savedOrder.getRestaurantName()).isEqualTo("Demo Restaurant");
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("71.00");

        assertThat(savedOrder.getItems()).hasSize(1);

        OrderItem savedItem = savedOrder.getItems().get(0);

        assertThat(savedItem.getMenuItemId()).isEqualTo(10L);
        assertThat(savedItem.getMenuItemName()).isEqualTo("Pizza");
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getUnitPrice()).isEqualByComparingTo("35.50");
        assertThat(savedItem.getSubtotal()).isEqualByComparingTo("71.00");
        assertThat(savedItem.getOrder()).isSameAs(savedOrder);
    }

    @Test
    void createOrder_shouldThrowBadRequestWhenMenuItemDoesNotBelongToRestaurant() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(1L)
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .menuItemId(999L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(menuClient.getRestaurantById(1L)).thenReturn(restaurant);

        assertThatThrownBy(() -> orderService.createOrder("customer@test.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to restaurant");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowResourceNotFoundWhenRestaurantIsNull() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(99L)
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .menuItemId(10L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(menuClient.getRestaurantById(99L)).thenReturn(null);

        assertThatThrownBy(() -> orderService.createOrder("customer@test.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrders_shouldReturnOnlyOrdersForCurrentUser() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.CREATED);

        when(orderRepository.findByUserEmailOrderByCreatedAtDesc("customer@test.com"))
                .thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getMyOrders("customer@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserEmail()).isEqualTo("customer@test.com");

        verify(orderRepository).findByUserEmailOrderByCreatedAtDesc("customer@test.com");
    }

    @Test
    void getOrderById_shouldAllowOwnerCustomer() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L, "customer@test.com", false);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserEmail()).isEqualTo("customer@test.com");
    }

    @Test
    void getOrderById_shouldRejectDifferentCustomer() {
        Order order = createOrderEntity(1L, "owner@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(1L, "other@test.com", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void getOrderById_shouldAllowAdminForAnyOrder() {
        Order order = createOrderEntity(1L, "owner@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L, "admin@test.com", true);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserEmail()).isEqualTo("owner@test.com");
    }

    @Test
    void updateOrderStatus_shouldChangeCreatedToConfirmed() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateOrderStatus_shouldRejectInvalidTransitionFromCreatedToCompleted() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.COMPLETED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid order status transition");
    }

    @Test
    void cancelOrder_shouldCancelOwnOrder() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelOrder(1L, "customer@test.com", false);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_shouldRejectDifferentCustomer() {
        Order order = createOrderEntity(1L, "owner@test.com", OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "other@test.com", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void cancelOrder_shouldRejectCompletedOrder() {
        Order order = createOrderEntity(1L, "customer@test.com", OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "customer@test.com", false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Completed orders cannot be cancelled");
    }

    private Order createOrderEntity(Long id, String userEmail, OrderStatus status) {
        Order order = Order.builder()
                .id(id)
                .userEmail(userEmail)
                .restaurantId(1L)
                .restaurantName("Demo Restaurant")
                .status(status)
                .totalPrice(BigDecimal.valueOf(35.50))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .menuItemId(10L)
                .menuItemName("Pizza")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(35.50))
                .subtotal(BigDecimal.valueOf(35.50))
                .build();

        order.addItem(item);

        return order;
    }
}