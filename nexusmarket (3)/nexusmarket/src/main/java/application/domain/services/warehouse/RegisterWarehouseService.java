package application.domain.services.warehouse;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.WarehouseStatus;
import application.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Warehouse execute(User requestingUser, Warehouse warehouse) {
        authorizeAdministrationService.execute(requestingUser);
        validateRequiredData(warehouse);
        resolveOwner(warehouse);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        Warehouse saved = warehouseRepositoryPort.save(warehouse);
        Map<String, Object> details = new HashMap<>();
        details.put("name", saved.getName());
        details.put("type", saved.getType().getCode());
        if (saved.getOwner() != null) {
            details.put("sellerIdentification", saved.getOwner().getIdentification());
        }
        registerOperationAndAuditService.execute(requestingUser, OperationType.WAREHOUSE_REGISTRATION, saved, details);
        return saved;
    }

    private void validateRequiredData(Warehouse warehouse) {
        if (warehouse.getName() == null || warehouse.getName().isBlank()) {
            throw new DomainException("The warehouse name must not be empty.");
        }
        if (warehouse.getAddress() == null || warehouse.getAddress().isBlank()) {
            throw new DomainException("The warehouse address is required.");
        }
        if (warehouse.getType() == null) {
            throw new DomainException("The warehouse type is required.");
        }
    }

    // Marketplace warehouses have no owner; seller warehouses require an existing, active seller.
    private void resolveOwner(Warehouse warehouse) {
        if (WarehouseType.MARKETPLACE.equals(warehouse.getType())) {
            warehouse.setOwner(null);
            return;
        }
        if (warehouse.getOwner() == null) {
            throw new DomainException("A seller warehouse requires an owner.");
        }
        Seller owner = sellerRepositoryPort.findByIdentification(warehouse.getOwner())
                .orElseThrow(() -> new EntityNotFoundException("Warehouse owner"));
        if (!SellerStatus.ACTIVE.equals(owner.getStatus())) {
            throw new DomainException("The seller must be active to register a warehouse.");
        }
        warehouse.setOwner(owner);
    }
}
