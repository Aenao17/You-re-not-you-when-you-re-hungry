package nttdata.orderservice.client;

import nttdata.orderservice.dto.external.UserResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class FakeUserClient implements UserClient {

    @Override
    public UserResponse getUserByUsername(String username) {
        return new UserResponse(
                1L,
                username,
                username,
                "CUSTOMER"
        );
    }
}
