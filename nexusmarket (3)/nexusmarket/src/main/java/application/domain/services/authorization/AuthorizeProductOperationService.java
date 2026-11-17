package application.domain.services.authorization;

import application.domain.models.Product;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Product registration and maintenance belongs exclusively to the owning seller. */
@Service
@RequiredArgsConstructor
public class AuthorizeProductOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public void execute(User user, Product product) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.SELLER);
        validateSellerOwnershipService.execute(user, product.getSeller());
    }
}
