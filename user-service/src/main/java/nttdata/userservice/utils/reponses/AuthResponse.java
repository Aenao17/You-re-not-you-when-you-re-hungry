package nttdata.userservice.utils.reponses;

public record AuthResponse(
        String accessToken,
        String tokenType
) {}