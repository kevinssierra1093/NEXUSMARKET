package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ShipmentEntity;
import application.adapters.out.persistence.mysql.mappers.CommercialPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.ShipmentJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class ShipmentMySqlAdapter implements ShipmentRepositoryPort {

    private final ShipmentJpaRepository shipmentJpaRepository;
    private final JpaReferences references;

    @Override
    public Shipment save(Shipment shipment) {
        if (shipment.getIdentifier() == null) {
            shipment.setIdentifier(JpaReferences.newIdentifier());
        }
        ShipmentEntity entity = new ShipmentEntity();
        entity.setIdentifier(shipment.getIdentifier());
        entity.setOrder(references.order(shipment.getOrder()));
        entity.setOriginWarehouse(references.warehouse(shipment.getOriginWarehouse()));
        apply(shipment, entity);
        shipmentJpaRepository.save(entity);
        return shipment;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shipment> findByIdentifier(Shipment shipment) {
        if (shipment == null || shipment.getIdentifier() == null) {
            return Optional.empty();
        }
        return shipmentJpaRepository.findById(shipment.getIdentifier()).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findByOrder(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return List.of();
        }
        return shipmentJpaRepository.findByOrder_Identifier(order.getIdentifier()).stream()
                .map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Shipment shipment) {
        ShipmentEntity entity = shipmentJpaRepository.findById(shipment.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Shipment"));
        apply(shipment, entity);
        shipmentJpaRepository.save(entity);
    }

    private void apply(Shipment shipment, ShipmentEntity entity) {
        CommercialPersistenceMapper.copy(shipment, entity);
        entity.setHandledBy(references.user(shipment.getHandledBy()));
    }
}
