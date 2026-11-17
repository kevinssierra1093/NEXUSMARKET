package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Internal roles (logistics operator, administrator, supervisor) are created by an administrator. */
@Service
@RequiredArgsConstructor
public class RegisterStaffUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public User execute(User requestingUser, User user) {
        authorizeAdministrationService.execute(requestingUser);
        validateStaffRole(user);
        validateUniqueness(user);
        user.setParticipant(null);
        user.setPassword(passwordServicePort.encrypt(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepositoryPort.save(user);
        Map<String, Object> details = new HashMap<>();
        details.put("username", saved.getUsername());
        details.put("role", saved.getRole().getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.USER_REGISTRATION, null, details);
        return saved;
    }

    private void validateStaffRole(User user) {
        boolean staff = SystemRole.LOGISTICS_OPERATOR.equals(user.getRole())
                || SystemRole.ADMINISTRATOR.equals(user.getRole())
                || SystemRole.SUPERVISOR.equals(user.getRole());
        if (!staff) {
            throw new DomainException("Staff users must have the role LOGISTICS_OPERATOR, ADMINISTRATOR or SUPERVISOR.");
        }
    }

    // Identification and email must be unique across the whole platform.
    private void validateUniqueness(User user) {
        if (userRepositoryPort.existsByUsername(user)) {
            throw new DomainException("Username " + user.getUsername() + " is already in use.");
        }
        if (userRepositoryPort.existsByIdentification(user) || buyerRepositoryPort.existsByIdentification(user)
                || sellerRepositoryPort.existsByIdentification(user)) {
            throw new DomainException("A person with identification " + user.getIdentification() + " already exists.");
        }
        if (userRepositoryPort.existsByEmail(user) || buyerRepositoryPort.existsByEmail(user)
                || sellerRepositoryPort.existsByEmail(user)) {
            throw new DomainException("A person with email " + user.getEmail() + " already exists.");
        }
    }
}
