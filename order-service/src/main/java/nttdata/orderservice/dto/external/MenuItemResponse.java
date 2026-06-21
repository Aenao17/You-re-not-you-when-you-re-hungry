package nttdata.orderservice.dto.external;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,

        String name,

        BigDecimal price
) {
}