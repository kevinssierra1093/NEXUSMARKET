package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InventoryItemEntity;
import application.adapters.out.persistence.mysql.entities.InventoryMovementEntity;
import application.adapters.out.persistence.mysql.entities.ProductEntity;
import application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Warehouse;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.WarehouseStatus;
import application.domain.valueobjects.WarehouseType;

import java.util.HashMap;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Warehouse, Product, ProductVariant, InventoryItem and InventoryMovement <-> JPA entities. */
public final class CatalogPersistenceMapper {

    private CatalogPersistenceMapper() {
    }

    // ------------------------------------------------------------- Warehouse

    public static Warehouse toDomain(WarehouseEntity entity) {
        if (entity == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(entity.getIdentifier());
        warehouse.setName(entity.getName());
        warehouse.setAddress(entity.getAddress());
        warehouse.setType(fromCode(WarehouseType.class, entity.getType()));
        warehouse.setOwner(ParticipantPersistenceMapper.toDomain(entity.getOwner()));
        warehouse.setStatus(fromCode(WarehouseStatus.class, entity.getStatus()));
        return warehouse;
    }

    public static void copy(Warehouse warehouse, WarehouseEntity entity) {
        entity.setName(warehouse.getName());
        entity.setAddress(warehouse.getAddress());
        entity.setType(code(warehouse.getType()));
        entity.setStatus(code(warehouse.getStatus()));
    }

    // --------------------------------------------------------------- Product

    /** Variants are not loaded here (ConsultProductService loads them through its own port). */
    public static Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        Product product = new Product();
        product.setIdentifier(entity.getIdentifier());
        product.setSeller(ParticipantPersistenceMapper.toDomain(entity.getSeller()));
        product.setName(entity.getName());
        product.setDescription(entity.getDescription());
        product.setType(fromCode(ProductType.class, entity.getType()));
        product.setStatus(fromCode(ProductStatus.class, entity.getStatus()));
        return product;
    }

    public static void copy(Product product, ProductEntity entity) {
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setType(code(product.getType()));
        entity.setStatus(code(product.getStatus()));
    }

    // -------------------------------------------------------- ProductVariant

    public static ProductVariant toDomain(ProductVariantEntity entity) {
        if (entity == null) {
            return null;
        }
        ProductVariant variant = new ProductVariant();
        variant.setIdentifier(entity.getIdentifier());
        variant.setProduct(toDomain(entity.getProduct()));
        variant.setAttributes(new HashMap<>(entity.getAttributes()));
        variant.setPrice(entity.getPrice());
        return variant;
    }

    public static void copy(ProductVariant variant, ProductVariantEntity entity) {
        entity.getAttributes().clear();
        if (variant.getAttributes() != null) {
            entity.getAttributes().putAll(variant.getAttributes());
        }
        entity.setPrice(variant.getPrice());
    }

    // --------------------------------------------------------- InventoryItem

    public static InventoryItem toDomain(InventoryItemEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryItem item = new InventoryItem();
        item.setIdentifier(entity.getIdentifier());
        item.setVariant(toDomain(entity.getVariant()));
        item.setWarehouse(toDomain(entity.getWarehouse()));
        item.setQuantityAvailable(entity.getQuantityAvailable());
        item.setQuantityReserved(entity.getQuantityReserved());
        item.setStatus(fromCode(InventoryItemStatus.class, entity.getStatus()));
        return item;
    }

    public static void copy(InventoryItem item, InventoryItemEntity entity) {
        entity.setQuantityAvailable(item.getQuantityAvailable());
        entity.setQuantityReserved(item.getQuantityReserved());
        entity.setStatus(code(item.getStatus()));
    }

    // ----------------------------------------------------- InventoryMovement

    public static InventoryMovement toDomain(InventoryMovementEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementId(entity.getMovementId());
        movement.setInventoryItem(toDomain(entity.getInventoryItem()));
        movement.setMovementType(fromCode(InventoryMovementType.class, entity.getMovementType()));
        movement.setQuantity(entity.getQuantity());
        movement.setMovementDate(entity.getMovementDate());
        movement.setPerformedBy(ParticipantPersistenceMapper.toDomain(entity.getPerformedBy()));
        if (entity.getRelatedOrder() != null) {
            // Reference only: the full order is available through OrderRepositoryPort.
            Order order = new Order();
            order.setIdentifier(entity.getRelatedOrder().getIdentifier());
            order.setOrderStatus(fromCode(OrderStatus.class, entity.getRelatedOrder().getOrderStatus()));
            movement.setRelatedOrder(order);
        }
        return movement;
    }
}
