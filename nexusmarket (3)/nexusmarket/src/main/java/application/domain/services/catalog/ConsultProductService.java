package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    // Published products are visible to everyone; the rest only to the owner and administrative roles.
    public Product execute(User requestingUser, Product product) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        if (!ProductStatus.PUBLISHED.equals(stored.getStatus())) {
            if (SystemRole.SELLER.equals(requestingUser.getRole())) {
                validateSellerOwnershipService.execute(requestingUser, stored.getSeller());
            } else {
                validateRoleAuthorizationService.execute(requestingUser,
                        SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
            }
        }
        stored.setVariants(productVariantRepositoryPort.findByProduct(stored));
        return stored;
    }
}
