package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChangeUserStatusService {

    private final UserRepositoryPort userRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public User execute(User requestingUser, User user, UserStatus newStatus) {
        authorizeAdministrationService.execute(requestingUser);
        User stored = userRepositoryPort.findById(user)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        if (Objects.equals(requestingUser.getUserId(), stored.getUserId())) {
            throw new DomainException("An administrator cannot change the status of their own user.");
        }
        if (stored.getStatus().equals(newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        userRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("username", stored.getUsername());
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.USER_STATUS_CHANGE, null, details);
        return stored;
    }
}
