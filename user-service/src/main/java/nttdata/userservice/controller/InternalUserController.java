package nttdata.userservice.controller;

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
public class InternalUserController {

    private final UserServiceImpl userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserSummaryDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserSummaryByUsername(username));
    }
}
