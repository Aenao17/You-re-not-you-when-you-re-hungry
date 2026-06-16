package nttdata.orderservice.dto.external;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}
