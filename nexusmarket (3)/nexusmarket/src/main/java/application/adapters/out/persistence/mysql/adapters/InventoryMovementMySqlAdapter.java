package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.InventoryMovementEntity;
import application.adapters.out.persistence.mysql.mappers.CatalogPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryMovementJpaRepository;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.code;

/** Movements are immutable: there is no update. */
@Component
@Transactional
@RequiredArgsConstructor
public class InventoryMovementMySqlAdapter implements InventoryMovementRepositoryPort {

    private final InventoryMovementJpaRepository inventoryMovementJpaRepository;
    private final JpaReferences references;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setInventoryItem(references.inventoryItem(movement.getInventoryItem()));
        entity.setMovementType(code(movement.getMovementType()));
        entity.setQuantity(movement.getQuantity());
        entity.setMovementDate(movement.getMovementDate());
        entity.setPerformedBy(references.user(movement.getPerformedBy()));
        entity.setRelatedOrder(references.order(movement.getRelatedOrder()));
        InventoryMovementEntity saved = inventoryMovementJpaRepository.save(entity);
        movement.setMovementId(saved.getMovementId());
        return movement;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovement> findByInventoryItem(InventoryItem item) {
        if (item == null || item.getIdentifier() == null) {
            return List.of();
        }
        return inventoryMovementJpaRepository.findByInventoryItem_IdentifierOrderByMovementDateAsc(item.getIdentifier())
                .stream().map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovement> findByOrder(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return List.of();
        }
        return inventoryMovementJpaRepository.findByRelatedOrder_IdentifierOrderByMovementDateAsc(order.getIdentifier())
                .stream().map(CatalogPersistenceMapper::toDomain).toList();
    }
}
