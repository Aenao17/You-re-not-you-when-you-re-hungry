package nttdata.menuservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "Numele preparatului este obligatoriu")
        String name,

        @NotNull(message = "Prețul este obligatoriu")
        @Positive(message = "Prețul trebuie să fie mai mare decât 0")
        BigDecimal price
) {
}