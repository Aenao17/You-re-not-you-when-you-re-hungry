package nttdata.menuservice.service;

import jakarta.persistence.EntityNotFoundException;
import nttdata.menuservice.dto.RestaurantRequest;
import nttdata.menuservice.dto.RestaurantResponse;
import nttdata.menuservice.model.Restaurant;
import nttdata.menuservice.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private RestaurantRequest testRequest;

    @BeforeEach
    void setUp() {
        // Configurăm datele de test care vor fi refolosite
        testRestaurant = Restaurant.builder()
                .restaurantId(1L)
                .name("Test Burger")
                .description("Cei mai buni burgeri")
                .menuItems(new ArrayList<>())
                .build();

        testRequest = new RestaurantRequest("Test Burger", "Cei mai buni burgeri");
    }

    @Test
    void createRestaurant_ShouldReturnRestaurantResponse() {
        // Arrange: Când repository-ul apelează save(), returnează obiectul nostru de test
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act: Apelăm metoda din service
        RestaurantResponse response = restaurantService.createRestaurant(testRequest);

        // Assert: Verificăm că rezultatele sunt corecte
        assertNotNull(response);
        assertEquals("Test Burger", response.name());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class)); // Verificăm că s-a apelat baza de date o singură dată
    }

    @Test
    void getRestaurantById_WhenExists_ShouldReturnResponse() {
        // Arrange
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // Act
        RestaurantResponse response = restaurantService.getRestaurantById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.restaurantId());
    }

    @Test
    void getRestaurantById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> restaurantService.getRestaurantById(99L));
    }

    @Test
    void getAllRestaurants_ShouldReturnList() {
        // Arrange
        when(restaurantRepository.findAll()).thenReturn(List.of(testRestaurant));

        // Act
        List<RestaurantResponse> responses = restaurantService.getAllRestaurants();

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Test Burger", responses.get(0).name());
    }

    @Test
    void updateRestaurant_WhenExists_ShouldReturnUpdatedResponse() {
        // Arrange
        RestaurantRequest updateRequest = new RestaurantRequest("Burger Modificat", "O nouă descriere");
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // Când se apelează save, îi spunem să returneze exact obiectul care a fost trimis spre salvare
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RestaurantResponse response = restaurantService.updateRestaurant(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Burger Modificat", response.name());
        assertEquals("O nouă descriere", response.description());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void deleteRestaurant_WhenExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(restaurantRepository.existsById(1L)).thenReturn(true);

        // Act
        restaurantService.deleteRestaurant(1L);

        // Assert
        verify(restaurantRepository, times(1)).existsById(1L);
        verify(restaurantRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRestaurant_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(restaurantRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> restaurantService.deleteRestaurant(99L));
        // Ne asigurăm că metoda deleteById nu a fost apelată din greșeală
        verify(restaurantRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchRestaurants_ShouldReturnMatchingList() {
        // Arrange
        String keyword = "Burger";
        when(restaurantRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword))
                .thenReturn(List.of(testRestaurant));

        // Act
        List<RestaurantResponse> responses = restaurantService.searchRestaurants(keyword);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Test Burger", responses.get(0).name());
        verify(restaurantRepository, times(1)).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
}