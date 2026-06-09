package nttdata.menuservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nttdata.menuservice.dto.MenuItemRequest;
import nttdata.menuservice.dto.MenuItemResponse;
import nttdata.menuservice.service.MenuItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // Ruta logică pentru adăugarea unui item într-un restaurant anume
    @PostMapping("/api/menu/restaurants/{restaurantId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> addMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addMenuItemToRestaurant(restaurantId, request));
    }

    // Ruta de bază pentru operațiunile pe un singur item
    @PutMapping("/api/menu/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long itemId, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, request));
    }

    @DeleteMapping("/api/menu/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/menu/items/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
    }
}