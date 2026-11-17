package application.domain.services.authorization;

import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Administrator-only operations: sellers, warehouses, users, refunds. */
@Service
@RequiredArgsConstructor
public class AuthorizeAdministrationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public void execute(User user) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.ADMINISTRATOR);
    }
}
