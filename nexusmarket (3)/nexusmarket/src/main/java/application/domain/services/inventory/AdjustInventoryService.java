package application.domain.services.inventory;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Manual correction of the available quantity. Negative stock is never permitted. */
@Service
@RequiredArgsConstructor
public class AdjustInventoryService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final AuthorizeInventoryOperationService authorizeInventoryOperationService;
    private final RecordInventoryMovementService recordInventoryMovementService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public InventoryItem execute(User requestingUser, InventoryItem item, Integer newQuantityAvailable) {
        InventoryItem stored = inventoryItemRepositoryPort.findByIdentifier(item)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
        authorizeInventoryOperationService.execute(requestingUser, stored.getVariant());
        if (newQuantityAvailable == null || newQuantityAvailable < 0) {
            throw new DomainException("Inventory can never be negative.");
        }
        int before = stored.getQuantityAvailable();
        if (before == newQuantityAvailable) {
            throw new DomainException("The adjustment does not change the available quantity.");
        }
        stored.setQuantityAvailable(newQuantityAvailable);
        inventoryItemRepositoryPort.update(stored);
        recordInventoryMovementService.execute(requestingUser, stored, InventoryMovementType.ADJUSTMENT,
                Math.abs(newQuantityAvailable - before), null);
        Map<String, Object> details = new HashMap<>();
        details.put("availableBefore", before);
        details.put("availableAfter", newQuantityAvailable);
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVENTORY_ADJUSTMENT, stored, details);
        return stored;
    }
}
