package nttdata.menuservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(
        @NotBlank(message = "Numele restaurantului este obligatoriu")
        @Size(max = 255, message = "Numele este prea lung")
        String name,

        @Size(max = 500, message = "Descrierea nu poate depăși 500 de caractere")
        String description
) {
}