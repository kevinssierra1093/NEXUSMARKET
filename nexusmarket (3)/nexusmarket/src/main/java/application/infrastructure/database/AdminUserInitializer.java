package application.infrastructure.database;

import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Staff users can only be created by an administrator (RegisterStaffUserService), so the first
 * ADMINISTRATOR is created here on startup when it does not exist yet.
 */
@Slf4j
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final String username;
    private final String password;
    private final String identification;
    private final String fullName;
    private final String email;

    public AdminUserInitializer(UserRepositoryPort userRepositoryPort,
                                PasswordServicePort passwordServicePort,
                                @Value("${nexusmarket.admin.username}") String username,
                                @Value("${nexusmarket.admin.password}") String password,
                                @Value("${nexusmarket.admin.identification}") String identification,
                                @Value("${nexusmarket.admin.full-name}") String fullName,
                                @Value("${nexusmarket.admin.email}") String email) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.username = username;
        this.password = password;
        this.identification = identification;
        this.fullName = fullName;
        this.email = email;
    }

    @Override
    public void run(String... args) {
        User criteria = new User();
        criteria.setUsername(username);
        if (userRepositoryPort.existsByUsername(criteria)) {
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordServicePort.encrypt(password));
        admin.setIdentification(identification);
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setRole(SystemRole.ADMINISTRATOR);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setParticipant(null);
        userRepositoryPort.save(admin);
        log.info("Initial administrator '{}' created. Change its password after the first login.", username);
    }
}
