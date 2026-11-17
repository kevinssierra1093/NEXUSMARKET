package application.domain.services.inventory;

import application.domain.exceptions.DomainException;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal domain service invoked by ConfirmOrderService (already authorized). Reserves
 * stock from a warehouse that has enough AVAILABLE units: non-existent or DAMAGED stock
 * can never be reserved.
 */
@Service
@RequiredArgsConstructor
public class ReserveInventoryService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final RecordInventoryMovementService recordInventoryMovementService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public InventoryItem execute(User requestingUser, ProductVariant variant, Integer quantity, Order order) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("The quantity to reserve must be greater than zero.");
        }
        InventoryItem item = inventoryItemRepositoryPort.findByVariant(variant).stream()
                .filter(candidate -> InventoryItemStatus.AVAILABLE.equals(candidate.getStatus()))
                .filter(candidate -> WarehouseStatus.ACTIVE.equals(candidate.getWarehouse().getStatus()))
                .filter(candidate -> candidate.getQuantityAvailable() >= quantity)
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Insufficient available inventory to reserve " + quantity
                                + " unit(s) of variant " + variant.getIdentifier() + "."));
        item.setQuantityAvailable(item.getQuantityAvailable() - quantity);
        item.setQuantityReserved(item.getQuantityReserved() + quantity);
        inventoryItemRepositoryPort.update(item);
        recordInventoryMovementService.execute(requestingUser, item, InventoryMovementType.RESERVATION, quantity, order);
        Map<String, Object> details = new HashMap<>();
        details.put("quantity", quantity);
        details.put("order", order.getIdentifier());
        details.put("warehouse", item.getWarehouse().getIdentifier());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVENTORY_RESERVATION, item, details);
        return item;
    }
}
