package nttdata.orderservice.dto.response;

import lombok.*;
import nttdata.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private String username;

    private Long restaurantId;

    private String restaurantName;

    private List<OrderItemResponse> items;

    private BigDecimal totalPrice;

    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}