package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Internal domain service invoked by ConfirmReturnReceptionService: returned units re-enter the warehouse. */
@Service
@RequiredArgsConstructor
public class RegisterInventoryReturnService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final RecordInventoryMovementService recordInventoryMovementService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public InventoryItem execute(User requestingUser, OrderItem line, Order order) {
        InventoryItem criteria = new InventoryItem();
        criteria.setVariant(line.getVariant());
        criteria.setWarehouse(line.getWarehouse());
        InventoryItem item = inventoryItemRepositoryPort.findByVariantAndWarehouse(criteria)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
        item.setQuantityAvailable(item.getQuantityAvailable() + line.getQuantity());
        inventoryItemRepositoryPort.update(item);
        recordInventoryMovementService.execute(requestingUser, item, InventoryMovementType.RETURN,
                line.getQuantity(), order);
        Map<String, Object> details = new HashMap<>();
        details.put("quantity", line.getQuantity());
        details.put("order", order.getIdentifier());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVENTORY_RETURN, item, details);
        return item;
    }
}
