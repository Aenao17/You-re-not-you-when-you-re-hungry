package nttdata.userservice.utils.dtos;

public record UserDto(Long id, String email, String username, String password, String role, String firstName, String lastName, String phoneNumber) {
}
