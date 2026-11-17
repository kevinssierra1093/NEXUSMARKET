package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OrderEntity;
import application.adapters.out.persistence.mysql.entities.OrderItemEntity;
import application.adapters.out.persistence.mysql.mappers.CommercialPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.valueobjects.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static application.adapters.shared.ValueObjectResolver.code;

/**
 * Persists the order together with its lines. The cart is the buyer's order in CART status.
 * Lines are identified by their product variant (a variant appears at most once per order).
 */
@Component
@Transactional
@RequiredArgsConstructor
public class OrderMySqlAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final JpaReferences references;

    @Override
    public Order save(Order order) {
        if (order.getIdentifier() == null) {
            order.setIdentifier(JpaReferences.newIdentifier());
        }
        OrderEntity entity = new OrderEntity();
        entity.setIdentifier(order.getIdentifier());
        entity.setBuyer(references.buyer(order.getBuyer()));
        CommercialPersistenceMapper.copy(order, entity);
        synchronizeItems(order, entity);
        orderJpaRepository.save(entity);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByIdentifier(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return Optional.empty();
        }
        return orderJpaRepository.findById(order.getIdentifier()).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findCartByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return Optional.empty();
        }
        return orderJpaRepository.findFirstByBuyer_IdentificationAndOrderStatus(
                buyer.getIdentification(), code(OrderStatus.CART)).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getIdentification() == null) {
            return List.of();
        }
        return orderJpaRepository.findByBuyer_IdentificationOrderByCreationDateDesc(buyer.getIdentification()).stream()
                .map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(Order order) {
        return orderJpaRepository.findByOrderStatusOrderByCreationDateAsc(code(order.getOrderStatus())).stream()
                .map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream().map(CommercialPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Order order) {
        OrderEntity entity = orderJpaRepository.findById(order.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        CommercialPersistenceMapper.copy(order, entity);
        synchronizeItems(order, entity);
        orderJpaRepository.save(entity);
    }

    /** Updates existing lines, adds new ones and removes the lines no longer present (orphan removal). */
    private void synchronizeItems(Order order, OrderEntity entity) {
        Map<String, OrderItemEntity> existing = new HashMap<>();
        for (OrderItemEntity line : entity.getItems()) {
            existing.put(line.getVariant().getIdentifier(), line);
        }
        Map<String, OrderItem> wanted = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            wanted.put(item.getVariant().getIdentifier(), item);
        }
        entity.getItems().removeIf(line -> !wanted.containsKey(line.getVariant().getIdentifier()));
        for (Map.Entry<String, OrderItem> entry : wanted.entrySet()) {
            OrderItem item = entry.getValue();
            OrderItemEntity line = existing.get(entry.getKey());
            if (line == null) {
                line = new OrderItemEntity();
                line.setOrder(entity);
                line.setVariant(references.variant(item.getVariant()));
                entity.getItems().add(line);
            }
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());
            line.setWarehouse(references.warehouse(item.getWarehouse()));
        }
    }
}
