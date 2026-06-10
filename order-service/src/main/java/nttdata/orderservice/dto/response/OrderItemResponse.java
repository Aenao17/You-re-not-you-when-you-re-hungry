package nttdata.orderservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;

    private Long menuItemId;

    private String menuItemName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}