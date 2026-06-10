package nttdata.userservice.service;

import nttdata.userservice.model.Role;
import nttdata.userservice.model.User;
import nttdata.userservice.repository.UserRepository;
import nttdata.userservice.utils.dtos.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("ana@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.CUSTOMER);
        user.setFirstName("Ana");
        user.setLastName("Popescu");
        user.setPhoneNumber("0712345678");

        userDto = new UserDto(
                1L,
                "ana@example.com",
                "ana",
                "password123",
                "CUSTOMER",
                "Ana",
                "Popescu",
                "0712345678"
        );
    }

    @Test
    void save_shouldEncodePasswordSetCustomerRoleAndReturnSavedUser() {
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        UserDto result = userService.save(userDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ana@example.com", result.email());
        assertEquals("CUSTOMER", result.role());
        assertEquals("Ana", result.firstName());
        assertEquals("Popescu", result.lastName());
        assertEquals("0712345678", result.phoneNumber());

        verify(passwordEncoder).encode("password123");

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("encodedPassword")
                        && savedUser.getRole() == Role.CUSTOMER
                        && savedUser.getCreatedAt() != null
                        && savedUser.getUpdatedAt() != null
        ));
    }

    @Test
    void getCurrentUser_shouldReturnUserDto_whenUsernameExists() {
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));

        UserDto result = userService.getCurrentUser("ana");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ana@example.com", result.email());
        assertEquals("CUSTOMER", result.role());
        assertEquals("Ana", result.firstName());

        verify(userRepository).findByUsername("ana");
    }

    @Test
    void getCurrentUser_shouldThrowNoSuchElementException_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getCurrentUser("missing")
        );

        assertEquals("User with username missing was not found", exception.getMessage());

        verify(userRepository).findByUsername("missing");
    }

    @Test
    void getUserById_shouldReturnUserDto_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ana@example.com", result.email());
        assertEquals("Ana", result.firstName());
        assertEquals("Popescu", result.lastName());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrowNoSuchElementException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getUserById(99L)
        );

        assertEquals("User with id 99 was not found", exception.getMessage());

        verify(userRepository).findById(99L);
    }

    @Test
    void getUserByUsername_shouldReturnUserDto_whenUsernameExists() {
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByUsername("ana");

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ana@example.com", result.email());
        assertEquals("CUSTOMER", result.role());

        verify(userRepository).findByUsername("ana");
    }

    @Test
    void getUserByUsername_shouldThrowNoSuchElementException_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.getUserByUsername("missing")
        );

        assertEquals("User with username missing was not found", exception.getMessage());

        verify(userRepository).findByUsername("missing");
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDtos() {
        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setEmail("bogdan@example.com");
        secondUser.setUsername("bogdan");
        secondUser.setPassword("encodedPassword2");
        secondUser.setRole(Role.CUSTOMER);
        secondUser.setFirstName("Bogdan");
        secondUser.setLastName("Ionescu");
        secondUser.setPhoneNumber("0799999999");

        when(userRepository.findAll()).thenReturn(List.of(user, secondUser));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("ana@example.com", result.get(0).email());
        assertEquals("bogdan@example.com", result.get(1).email());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

}
