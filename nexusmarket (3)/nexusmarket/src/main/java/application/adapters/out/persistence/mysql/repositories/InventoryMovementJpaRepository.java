package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementEntity, Integer> {

    List<InventoryMovementEntity> findByInventoryItem_IdentifierOrderByMovementDateAsc(String inventoryItemIdentifier);

    List<InventoryMovementEntity> findByRelatedOrder_IdentifierOrderByMovementDateAsc(String orderIdentifier);
}
