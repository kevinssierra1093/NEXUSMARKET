package application.domain.services.authorization;

import application.domain.models.Order;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Order consultation: same actors as order management, plus read-only supervisors. */
@Service
@RequiredArgsConstructor
public class AuthorizeOrderConsultationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final AuthorizeOrderOperationService authorizeOrderOperationService;

    public void execute(User user, Order order) {
        validateUserAuthorizationStatusService.execute(user);
        if (SystemRole.SUPERVISOR.equals(user.getRole())) {
            return;
        }
        authorizeOrderOperationService.execute(user, order);
    }
}
