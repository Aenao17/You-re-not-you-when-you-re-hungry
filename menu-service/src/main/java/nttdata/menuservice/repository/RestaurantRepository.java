package nttdata.menuservice.repository;

import nttdata.menuservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Spring va genera automat query-ul:
    // SELECT * FROM restaurants WHERE LOWER(name) LIKE LOWER(%keyword%) OR LOWER(description) LIKE LOWER(%keyword%)
    List<Restaurant> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}