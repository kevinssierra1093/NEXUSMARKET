package application.domain.ports.out;

import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;

import java.util.List;

public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);

    List<InventoryMovement> findByInventoryItem(InventoryItem item);

    List<InventoryMovement> findByOrder(Order order);
}
