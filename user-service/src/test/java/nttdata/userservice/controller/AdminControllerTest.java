package nttdata.userservice.controller;

import nttdata.userservice.security.filter.JwtAuthFilter;
import nttdata.userservice.service.UserServiceImpl;
import nttdata.userservice.utils.dtos.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceImpl userService;

    @Test
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "ana@example.com",
                "ana",
                "password",
                "CUSTOMER",
                "Ana",
                "Popescu",
                "0712345678"
        );

        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Popescu"))
                .andExpect(jsonPath("$.phoneNumber").value("0712345678"));
    }

    @Test
    void getUserByUsername_shouldReturnUser_whenUserExists() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "ana@example.com",
                "password",
                "ana",
                "CUSTOMER",
                "Ana",
                "Popescu",
                "0712345678"
        );

        when(userService.getUserByUsername("ana")).thenReturn(userDto);

        mockMvc.perform(get("/api/admin/users/username/ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    void getAllUsers_shouldReturnUsersList() throws Exception {
        UserDto firstUser = new UserDto(
                1L,
                "ana@example.com",
                "password",
                "ana",
                "CUSTOMER",
                "Ana",
                "Popescu",
                "0712345678"
        );

        UserDto secondUser = new UserDto(
                2L,
                "bogdan@example.com",
                "password",
                "ana",
                "CUSTOMER",
                "Bogdan",
                "Ionescu",
                "0799999999"
        );

        when(userService.getAllUsers()).thenReturn(List.of(firstUser, secondUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$[1].email").value("bogdan@example.com"));
    }
}