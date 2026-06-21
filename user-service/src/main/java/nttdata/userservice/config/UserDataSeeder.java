package nttdata.userservice.config;

import lombok.RequiredArgsConstructor;
import nttdata.userservice.model.Role;
import nttdata.userservice.model.User;
import nttdata.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfNotExists(
                "admin1",
                "admin1@test.com",
                "Admin",
                "User",
                "0711111111",
                Role.ADMIN
        );

        createUserIfNotExists(
                "customer1",
                "customer1@test.com",
                "Customer",
                "One",
                "0722222222",
                Role.CUSTOMER
        );

        createUserIfNotExists(
                "customer2",
                "customer2@test.com",
                "Customer",
                "Two",
                "0733333333",
                Role.CUSTOMER
        );
    }

    private void createUserIfNotExists(
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            Role role
    ) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);
    }
}