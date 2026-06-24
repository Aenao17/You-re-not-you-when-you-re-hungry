package nttdata.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nttdata.userservice.service.UserServiceImpl;
import nttdata.userservice.utils.dtos.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for the currently authenticated user")
public class UserController {

    private final UserServiceImpl userService;

    @Operation(summary = "Health check", description = "Returns a simple greeting to confirm the user-service is reachable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is up")
    })
    @GetMapping("/hello")
    public String hello() {
        return "Hello from user-service";
    }

    @Operation(summary = "Get my profile", description = "Returns the full profile of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                userService.getCurrentUser(userDetails.getUsername())
        );
    }
}
