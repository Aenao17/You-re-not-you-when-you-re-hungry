package nttdata.menuservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nttdata.menuservice.dto.MenuItemRequest;
import nttdata.menuservice.dto.MenuItemResponse;
import nttdata.menuservice.model.MenuItem;
import nttdata.menuservice.model.Restaurant;
import nttdata.menuservice.repository.MenuItemRepository;
import nttdata.menuservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public MenuItemResponse addMenuItemToRestaurant(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurantul cu id-ul " + restaurantId + " nu a fost găsit."));

        MenuItem menuItem = MenuItem.builder()
                .name(request.name())
                .price(request.price())
                .build();

        // restaurant.addMenuItem(menuItem);
        // restaurantRepository.saveAndFlush(restaurant);

        // 2. Facem legătura bidirecțională manual, ca să fim siguri că obiectele din memorie sunt corecte
        menuItem.setRestaurant(restaurant); // Asigură-te că ai @Setter pe câmpul restaurant din MenuItem
        restaurant.addMenuItem(menuItem);

        // 3. REPARAȚIA: Salvăm direct produsul, NU restaurantul!
        // .save() pe repository-ul propriu returnează INSTANT obiectul cu ID-ul din baza de date
        MenuItem savedItem = menuItemRepository.save(menuItem);

        return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getPrice());
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Preparatul cu id-ul " + itemId + " nu a fost găsit."));

        menuItem.setName(request.name());
        menuItem.setPrice(request.price());

        MenuItem updatedItem = menuItemRepository.save(menuItem);

        return new MenuItemResponse(updatedItem.getId(), updatedItem.getName(), updatedItem.getPrice());
    }

    @Transactional
    public void deleteMenuItem(Long itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            throw new EntityNotFoundException("Preparatul cu id-ul " + itemId + " nu a fost găsit.");
        }
        menuItemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Preparatul cu id-ul " + itemId + " nu a fost găsit."));

        return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getPrice());
    }
}