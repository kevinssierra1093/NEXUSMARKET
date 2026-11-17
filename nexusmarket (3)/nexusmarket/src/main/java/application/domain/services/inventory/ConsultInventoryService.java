package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryConsultationService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConsultInventoryService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeInventoryConsultationService authorizeInventoryConsultationService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public List<InventoryItem> executeByVariant(User requestingUser, ProductVariant variant) {
        ProductVariant stored = productVariantRepositoryPort.findByIdentifier(variant)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant"));
        authorizeInventoryConsultationService.execute(requestingUser, stored);
        return inventoryItemRepositoryPort.findByVariant(stored);
    }

    // A seller only sees the stock of their own products, even inside shared warehouses.
    public List<InventoryItem> executeByWarehouse(User requestingUser, Warehouse warehouse) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.SELLER,
                SystemRole.LOGISTICS_OPERATOR, SystemRole.SUPERVISOR, SystemRole.ADMINISTRATOR);
        Warehouse stored = warehouseRepositoryPort.findByIdentifier(warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        List<InventoryItem> items = inventoryItemRepositoryPort.findByWarehouse(stored);
        if (SystemRole.SELLER.equals(requestingUser.getRole())) {
            return items.stream().filter(item -> requestingUser.getParticipant() != null
                    && Objects.equals(requestingUser.getParticipant().getIdentification(),
                    item.getVariant().getProduct().getSeller().getIdentification())).toList();
        }
        return items;
    }
}
