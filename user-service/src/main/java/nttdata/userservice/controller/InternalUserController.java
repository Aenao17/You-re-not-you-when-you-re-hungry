package nttdata.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nttdata.userservice.service.UserServiceImpl;
import nttdata.userservice.utils.dtos.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal Users", description = "Service-to-service user lookup — not exposed via the public API gateway")
public class InternalUserController {

    private final UserServiceImpl userService;

    @Operation(summary = "Get user summary by username", description = "Used internally by other microservices to resolve a username to a user summary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User summary returned successfully"),
            @ApiResponse(responseCode = "404", description = "No user found with the given username")
    })
    @GetMapping("/{username}")
    public ResponseEntity<UserSummaryDto> getUserByUsername(
            @Parameter(description = "The username to look up", required = true)
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserSummaryByUsername(username));
    }
}
