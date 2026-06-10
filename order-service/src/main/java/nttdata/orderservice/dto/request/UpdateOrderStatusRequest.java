package nttdata.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import nttdata.orderservice.entity.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}