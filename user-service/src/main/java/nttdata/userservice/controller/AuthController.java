package nttdata.userservice.controller;

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
public class AuthController {
    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDTO){
        UserDto newUser = userService.save(userDTO);
        return ResponseEntity.ok(newUser);
    }

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
