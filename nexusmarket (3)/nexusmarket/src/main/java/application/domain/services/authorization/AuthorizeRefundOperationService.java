package application.domain.services.authorization;

import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Returns and refunds (responsibility matrix): the buyer who owns the request, or an
 * administrator. Supervisors may consult.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeRefundOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public void execute(User user, Buyer buyer) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.BUYER, SystemRole.ADMINISTRATOR);
        if (SystemRole.BUYER.equals(user.getRole())) {
            validateBuyerOwnershipService.execute(user, buyer);
        }
    }

    public void executeConsultation(User user, Buyer buyer) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user,
                SystemRole.BUYER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        if (SystemRole.BUYER.equals(user.getRole())) {
            validateBuyerOwnershipService.execute(user, buyer);
        }
    }
}
