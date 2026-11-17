package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChangeUserPasswordService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User requestingUser, User user, String newPassword) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        User stored = userRepositoryPort.findById(user)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        boolean self = Objects.equals(requestingUser.getUserId(), stored.getUserId());
        if (!self && !SystemRole.ADMINISTRATOR.equals(requestingUser.getRole())) {
            throw new UnauthorizedOperationException("Only the user or an administrator can change the password.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new DomainException("The new password must not be empty.");
        }
        stored.setPassword(passwordServicePort.encrypt(newPassword));
        userRepositoryPort.update(stored);
    }
}
