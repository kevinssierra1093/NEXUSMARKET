package application.infrastructure.security;

import application.domain.exceptions.DomainException;
import application.domain.ports.out.PasswordServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Implements PasswordServicePort with BCrypt. */
@Component
@RequiredArgsConstructor
public class PasswordSecurityAdapter implements PasswordServicePort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return rawPassword != null && encodedPassword != null && passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("The password must not be empty.");
        }
        return passwordEncoder.encode(rawPassword);
    }
}
