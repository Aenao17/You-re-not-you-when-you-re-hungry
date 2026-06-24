package nttdata.menuservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Menu Items", description = "Manage menu items within restaurants — write operations require ADMIN role")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Operation(summary = "Add a menu item to a restaurant", description = "Creates a new menu item and associates it with the specified restaurant. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PostMapping("/api/menu/restaurants/{restaurantId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @Parameter(description = "Numeric restaurant ID", required = true)
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.addMenuItemToRestaurant(restaurantId, request));
    }

    @Operation(summary = "Update a menu item", description = "Replaces all fields of the specified menu item. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    @PutMapping("/api/menu/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @Parameter(description = "Numeric menu item ID", required = true)
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, request));
    }

    @Operation(summary = "Delete a menu item", description = "Permanently removes the specified menu item. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    @DeleteMapping("/api/menu/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(
            @Parameter(description = "Numeric menu item ID", required = true)
            @PathVariable Long itemId) {
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get menu item by ID", description = "Returns a single menu item by its numeric ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Menu item found"),
            @ApiResponse(responseCode = "404", description = "Menu item not found")
    })
    @GetMapping("/api/menu/items/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @Parameter(description = "Numeric menu item ID", required = true)
            @PathVariable Long itemId) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
    }
}
