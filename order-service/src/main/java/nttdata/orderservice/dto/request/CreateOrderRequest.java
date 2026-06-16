package nttdata.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<CreateOrderItemRequest> items;
}