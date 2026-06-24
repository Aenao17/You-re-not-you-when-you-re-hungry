package nttdata.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nttdata.userservice.service.JwtService;
import nttdata.userservice.service.UserServiceImpl;
import nttdata.userservice.utils.dtos.UserDto;
import nttdata.userservice.utils.reponses.AuthResponse;
import nttdata.userservice.utils.requests.AuthRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login endpoints — no authentication required")
public class AuthController {
    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "Register a new user", description = "Creates a new user account with the USER role by default.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDTO){
        UserDto newUser = userService.save(userDTO);
        return ResponseEntity.ok(newUser);
    }

    @Operation(summary = "Login and obtain JWT token", description = "Authenticates the user and returns a Bearer JWT access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public AuthResponse authenticateAndGenerateToken(@RequestBody AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            String accessToken = jwtService.generateToken(authRequest.getUsername());

            return new AuthResponse(
                    accessToken,
                    "Bearer"
            );
        } else {
            throw new UsernameNotFoundException("Invalid username or password");
        }
    }

}
