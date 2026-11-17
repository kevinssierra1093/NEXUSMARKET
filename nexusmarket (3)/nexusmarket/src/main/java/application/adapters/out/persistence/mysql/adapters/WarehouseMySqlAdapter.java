package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import application.adapters.out.persistence.mysql.mappers.CatalogPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class WarehouseMySqlAdapter implements WarehouseRepositoryPort {

    private final WarehouseJpaRepository warehouseJpaRepository;
    private final JpaReferences references;

    @Override
    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getIdentifier() == null) {
            warehouse.setIdentifier(JpaReferences.newIdentifier());
        }
        WarehouseEntity entity = new WarehouseEntity();
        entity.setIdentifier(warehouse.getIdentifier());
        apply(warehouse, entity);
        warehouseJpaRepository.save(entity);
        return warehouse;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Warehouse> findByIdentifier(Warehouse warehouse) {
        if (warehouse == null || warehouse.getIdentifier() == null) {
            return Optional.empty();
        }
        return warehouseJpaRepository.findById(warehouse.getIdentifier()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findBySeller(Seller seller) {
        if (seller == null || seller.getIdentification() == null) {
            return List.of();
        }
        return warehouseJpaRepository.findByOwner_Identification(seller.getIdentification()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findAll() {
        return warehouseJpaRepository.findAll().stream().map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Warehouse warehouse) {
        WarehouseEntity entity = warehouseJpaRepository.findById(warehouse.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        apply(warehouse, entity);
        warehouseJpaRepository.save(entity);
    }

    private void apply(Warehouse warehouse, WarehouseEntity entity) {
        CatalogPersistenceMapper.copy(warehouse, entity);
        entity.setOwner(references.seller(warehouse.getOwner()));
    }
}
