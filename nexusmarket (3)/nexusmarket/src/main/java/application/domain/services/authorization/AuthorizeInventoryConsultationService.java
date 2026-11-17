package application.domain.services.authorization;

import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Inventory consultation: owning seller, logistics operators and supervisors. */
@Service
@RequiredArgsConstructor
public class AuthorizeInventoryConsultationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public void execute(User user, ProductVariant variant) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user,
                SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR, SystemRole.SUPERVISOR);
        if (SystemRole.SELLER.equals(user.getRole())) {
            validateSellerOwnershipService.execute(user, variant.getProduct().getSeller());
        }
    }
}
