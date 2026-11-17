package application.domain.services.inventory;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.WarehouseStatus;
import application.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Registers stock entering a warehouse (initial stock or restock). Creates the record if absent. */
@Service
@RequiredArgsConstructor
public class RegisterInventoryInboundService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;
    private final RecordInventoryMovementService recordInventoryMovementService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public InventoryItem execute(User requestingUser, InventoryItem item, Integer quantity) {
        if (item.getVariant() == null || item.getWarehouse() == null) {
            throw new DomainException("Inventory must reference a product variant and a warehouse.");
        }
        ProductVariant variant = productVariantRepositoryPort.findByIdentifier(item.getVariant())
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant"));
        authorizeInventoryOperationService.execute(requestingUser, variant);
        Warehouse warehouse = warehouseRepositoryPort.findByIdentifier(item.getWarehouse())
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        validate(variant, warehouse, quantity);

        item.setVariant(variant);
        item.setWarehouse(warehouse);
        Optional<InventoryItem> existing = inventoryItemRepositoryPort.findByVariantAndWarehouse(item);
        InventoryItem target = existing.orElseGet(() -> newRecord(variant, warehouse));
        if (!InventoryItemStatus.AVAILABLE.equals(target.getStatus())) {
            throw new DomainException("Stock cannot be added to an inventory record marked as "
                    + target.getStatus().getCode() + ".");
        }
        int before = target.getQuantityAvailable();
        target.setQuantityAvailable(before + quantity);
        InventoryItem saved;
        if (existing.isPresent()) {
            inventoryItemRepositoryPort.update(target);
            saved = target;
        } else {
            saved = inventoryItemRepositoryPort.save(target);
        }
        recordInventoryMovementService.execute(requestingUser, saved, InventoryMovementType.INBOUND, quantity, null);
        Map<String, Object> details = new HashMap<>();
        details.put("quantity", quantity);
        details.put("availableBefore", before);
        details.put("availableAfter", saved.getQuantityAvailable());
        details.put("warehouse", warehouse.getIdentifier());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVENTORY_INBOUND, saved, details);
        return saved;
    }

    private void validate(ProductVariant variant, Warehouse warehouse, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("The inbound quantity must be greater than zero.");
        }
        if (!ProductType.PHYSICAL.equals(variant.getProduct().getType())) {
            throw new DomainException("Digital products do not require inventory.");
        }
        if (!WarehouseStatus.ACTIVE.equals(warehouse.getStatus())) {
            throw new DomainException("Stock cannot be registered in an inactive warehouse.");
        }
        boolean foreignSellerWarehouse = WarehouseType.SELLER.equals(warehouse.getType())
                && !Objects.equals(warehouse.getOwner().getIdentification(),
                variant.getProduct().getSeller().getIdentification());
        if (foreignSellerWarehouse) {
            throw new DomainException("A product cannot be stocked in a warehouse of another seller.");
        }
    }

    private InventoryItem newRecord(ProductVariant variant, Warehouse warehouse) {
        InventoryItem record = new InventoryItem();
        record.setVariant(variant);
        record.setWarehouse(warehouse);
        record.setQuantityAvailable(0);
        record.setQuantityReserved(0);
        record.setStatus(InventoryItemStatus.AVAILABLE);
        return record;
    }
}
