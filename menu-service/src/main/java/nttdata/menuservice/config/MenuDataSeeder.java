package nttdata.menuservice.config;

import lombok.RequiredArgsConstructor;
import nttdata.menuservice.model.MenuItem;
import nttdata.menuservice.model.Restaurant;
import nttdata.menuservice.repository.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class MenuDataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;

    @Override
    public void run(String... args) {
        if (restaurantRepository.count() > 0) {
            return;
        }

        Restaurant italianRestaurant = Restaurant.builder()
                .name("Demo Italian Restaurant")
                .description("Pizza, pasta and refreshing drinks")
                .build();

        italianRestaurant.addMenuItem(MenuItem.builder()
                .name("Pizza Margherita")
                .price(BigDecimal.valueOf(35.50))
                .build());

        italianRestaurant.addMenuItem(MenuItem.builder()
                .name("Pasta Carbonara")
                .price(BigDecimal.valueOf(42.00))
                .build());

        italianRestaurant.addMenuItem(MenuItem.builder()
                .name("Lemonade")
                .price(BigDecimal.valueOf(12.00))
                .build());

        Restaurant burgerHouse = Restaurant.builder()
                .name("Burger House")
                .description("Burgers, sides and soft drinks")
                .build();

        burgerHouse.addMenuItem(MenuItem.builder()
                .name("Classic Burger")
                .price(BigDecimal.valueOf(39.00))
                .build());

        burgerHouse.addMenuItem(MenuItem.builder()
                .name("French Fries")
                .price(BigDecimal.valueOf(15.00))
                .build());

        burgerHouse.addMenuItem(MenuItem.builder()
                .name("Cola")
                .price(BigDecimal.valueOf(10.00))
                .build());

        restaurantRepository.save(italianRestaurant);
        restaurantRepository.save(burgerHouse);
    }
}