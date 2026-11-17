package application.domain.services.inventory;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.valueobjects.InventoryMovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Persists the stock movement event that produced a change in an inventory record. */
@Service
@RequiredArgsConstructor
public class RecordInventoryMovementService {

    private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;

    public InventoryMovement execute(User user, InventoryItem item, InventoryMovementType type,
                                     Integer quantity, Order relatedOrder) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setMovementDate(LocalDateTime.now());
        movement.setPerformedBy(user);
        movement.setRelatedOrder(relatedOrder);
        return inventoryMovementRepositoryPort.save(movement);
    }
}
