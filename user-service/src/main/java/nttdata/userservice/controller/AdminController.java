package nttdata.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nttdata.userservice.service.UserServiceImpl;
import nttdata.userservice.utils.dtos.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints for managing all users — requires ADMIN role")
public class AdminController {

    private final UserServiceImpl userService;

    @Operation(summary = "Get user by ID", description = "Returns the full user profile for the given numeric ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user found with the given ID")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "Numeric user ID", required = true)
            @PathVariable Long id){
        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @Operation(summary = "Get user by username", description = "Returns the full user profile for the given username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No user found with the given username")
    })
    @GetMapping("/users/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "The username to look up", required = true)
            @PathVariable String username) {
        return ResponseEntity.ok(
                userService.getUserByUsername(username)
        );
    }

    @Operation(summary = "Get all users", description = "Returns a list of all registered users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ADMIN role")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

}
