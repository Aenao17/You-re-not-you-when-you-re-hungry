package nttdata.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nttdata.orderservice.dto.request.CreateOrderRequest;
import nttdata.orderservice.dto.request.UpdateOrderStatusRequest;
import nttdata.orderservice.dto.response.OrderResponse;
import nttdata.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place and manage food orders — customers manage their own orders, admins manage all orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order for the authenticated customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or invalid menu item"),
            @ApiResponse(responseCode = "404", description = "User or restaurant not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User does not have CUSTOMER role"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        String username = authentication.getName();

        OrderResponse response = orderService.createOrder(username, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get my orders", description = "Returns all orders created by the authenticated customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User does not have CUSTOMER role")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        String username = authentication.getName();

        return ResponseEntity.ok(orderService.getMyOrders(username));
    }

    @Operation(summary = "Get order by ID", description = "Returns an order by ID. Customers can only access their own orders. Admins can access any order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to access this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Numeric order ID", required = true)
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean isAdmin = hasAuthority(authentication, "ROLE_ADMIN");

        OrderResponse response = orderService.getOrderById(orderId, username, isAdmin);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all orders", description = "Returns all orders. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Update order status", description = "Updates the status of an order. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Numeric order ID", required = true)
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel order", description = "Cancels an order. Customers can cancel their own orders. Admins can cancel any order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to cancel this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Numeric order ID", required = true)
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        String username = authentication.getName();
        boolean isAdmin = hasAuthority(authentication, "ROLE_ADMIN");

        OrderResponse response = orderService.cancelOrder(orderId, username, isAdmin);

        return ResponseEntity.ok(response);
    }

    private boolean hasAuthority(Authentication authentication, String authorityName) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(authorityName));
    }
}