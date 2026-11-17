package application.domain.services.seller;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultSellerService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    // Warehouses and products are populated on demand here; empty by default in the model.
    public Seller execute(User requestingUser, Seller seller) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Seller stored = sellerRepositoryPort.findByIdentification(seller)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        if (SystemRole.SELLER.equals(requestingUser.getRole())) {
            validateSellerOwnershipService.execute(requestingUser, stored);
        }
        stored.setWarehouses(warehouseRepositoryPort.findBySeller(stored));
        stored.setProducts(productRepositoryPort.findBySeller(stored));
        return stored;
    }

    public List<Seller> executeAll(User requestingUser) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        return sellerRepositoryPort.findAll();
    }
}
