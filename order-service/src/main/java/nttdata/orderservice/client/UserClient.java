package nttdata.orderservice.client;

import nttdata.orderservice.dto.external.UserResponse;

public interface UserClient {

    UserResponse getUserByUsername(String username);
}
