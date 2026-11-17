package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.InventoryItem;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Marks stock as DAMAGED (unsellable) or back to AVAILABLE. */
@Service
@RequiredArgsConstructor
public class ChangeInventoryItemStatusService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;
    private final RecordInventoryMovementService recordInventoryMovementService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public InventoryItem execute(User requestingUser, InventoryItem item, InventoryItemStatus newStatus) {
        InventoryItem stored = inventoryItemRepositoryPort.findByIdentifier(item)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
        authorizeInventoryOperationService.execute(requestingUser, stored.getVariant());
        if (stored.getStatus().equals(newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        inventoryItemRepositoryPort.update(stored);
        recordInventoryMovementService.execute(requestingUser, stored, InventoryMovementType.ADJUSTMENT,
                stored.getQuantityAvailable(), null);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        details.put("affectedUnits", stored.getQuantityAvailable());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVENTORY_ADJUSTMENT, stored, details);
        return stored;
    }
}
