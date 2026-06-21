package nttdata.menuservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nttdata.menuservice.dto.MenuItemResponse;
import nttdata.menuservice.dto.RestaurantRequest;
import nttdata.menuservice.dto.RestaurantResponse;
import nttdata.menuservice.model.Restaurant;
import nttdata.menuservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = fetchRestaurantEntity(id);

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new EntityNotFoundException("Restaurantul cu id-ul " + id + " nu a fost găsit.");
        }
        restaurantRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String keyword) {
        return restaurantRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = fetchRestaurantEntity(id);
        return mapToResponse(restaurant);
    }

    protected Restaurant fetchRestaurantEntity(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurantul cu id-ul " + id + " nu a fost găsit."));
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        List<MenuItemResponse> menuItemsResponse = restaurant.getMenuItems().stream()
                .map(item -> new MenuItemResponse(item.getId(), item.getName(), item.getPrice()))
                .collect(Collectors.toList());

        return new RestaurantResponse(
                restaurant.getRestaurantId(),
                restaurant.getName(),
                restaurant.getDescription(),
                menuItemsResponse
        );
    }
}