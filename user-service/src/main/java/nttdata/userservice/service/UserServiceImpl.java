package nttdata.userservice.service;

import lombok.RequiredArgsConstructor;
import nttdata.userservice.model.Role;
import nttdata.userservice.model.User;
import nttdata.userservice.repository.UserRepository;
import nttdata.userservice.utils.dtos.UserDto;
import nttdata.userservice.utils.mappers.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto save(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(user.getPassword())));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(Role.CUSTOMER);

        User newUser = userRepository.save(user);
        return UserMapper.toUserDto(newUser);
    }

    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null){

            throw new NoSuchElementException(User.class.getSimpleName() + " with username " + username + " was not found");
        }
        return UserMapper.toUserDto(user);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null){
            throw new NoSuchElementException(User.class.getSimpleName() + " with id " + id + " was not found");
        }
        return UserMapper.toUserDto(user);
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null){
            throw new NoSuchElementException(User.class.getSimpleName() + " with username " + username + " was not found");
        }
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toUserDto).toList();
    }
}
