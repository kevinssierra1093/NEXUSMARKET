package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.InventoryItemEntity;
import application.adapters.out.persistence.mysql.mappers.CatalogPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryItemJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.ProductVariant;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class InventoryItemMySqlAdapter implements InventoryItemRepositoryPort {

    private final InventoryItemJpaRepository inventoryItemJpaRepository;
    private final JpaReferences references;

    @Override
    public InventoryItem save(InventoryItem item) {
        if (item.getIdentifier() == null) {
            item.setIdentifier(JpaReferences.newIdentifier());
        }
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setIdentifier(item.getIdentifier());
        apply(item, entity);
        inventoryItemJpaRepository.save(entity);
        return item;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByIdentifier(InventoryItem item) {
        if (item == null || item.getIdentifier() == null) {
            return Optional.empty();
        }
        return inventoryItemJpaRepository.findById(item.getIdentifier()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByVariantAndWarehouse(InventoryItem item) {
        if (item == null || item.getVariant() == null || item.getWarehouse() == null
                || item.getVariant().getIdentifier() == null || item.getWarehouse().getIdentifier() == null) {
            return Optional.empty();
        }
        return inventoryItemJpaRepository.findByVariant_IdentifierAndWarehouse_Identifier(
                        item.getVariant().getIdentifier(), item.getWarehouse().getIdentifier())
                .map(CatalogPersistenceMapper::toDomain);
    }

    /**
     * Used by ReserveInventoryService: the rows are locked (SELECT ... FOR UPDATE) until the end
     * of the surrounding transaction opened by the input adapter.
     */
    @Override
    public List<InventoryItem> findByVariant(ProductVariant variant) {
        if (variant == null || variant.getIdentifier() == null) {
            return List.of();
        }
        return inventoryItemJpaRepository.findByVariantForUpdate(variant.getIdentifier()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> findByWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getIdentifier() == null) {
            return List.of();
        }
        return inventoryItemJpaRepository.findByWarehouse_Identifier(warehouse.getIdentifier()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> findAll() {
        return inventoryItemJpaRepository.findAll().stream().map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(InventoryItem item) {
        InventoryItemEntity entity = inventoryItemJpaRepository.findById(item.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
        apply(item, entity);
        inventoryItemJpaRepository.save(entity);
    }

    private void apply(InventoryItem item, InventoryItemEntity entity) {
        CatalogPersistenceMapper.copy(item, entity);
        entity.setVariant(references.variant(item.getVariant()));
        entity.setWarehouse(references.warehouse(item.getWarehouse()));
    }
}
