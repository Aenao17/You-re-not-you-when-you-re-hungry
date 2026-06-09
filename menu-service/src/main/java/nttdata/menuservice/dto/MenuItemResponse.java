package nttdata.menuservice.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        BigDecimal price
) {
}