package nttdata.userservice.utils.mappers;

import nttdata.userservice.model.Role;
import nttdata.userservice.model.User;
import nttdata.userservice.utils.dtos.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
        public static UserDto toUserDto(User user) {
            return new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole().name(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber()
            );
        }

        public static User toUser(UserDto userDto) {
            User user = new User();
            user.setId(userDto.id());
            user.setFirstName(userDto.firstName());
            user.setLastName(userDto.lastName());
            user.setEmail(userDto.email());
            user.setPassword(userDto.password());
            try{
                user.setRole(Role.valueOf(userDto.role()));
            } catch (Exception e){
                user.setRole(Role.CUSTOMER);
            }
            user.setPhoneNumber(userDto.phoneNumber());
            user.setUsername(userDto.username());
            return user;
        }
}
