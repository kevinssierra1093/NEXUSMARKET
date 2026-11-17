package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.BuyerEntity;
import application.adapters.out.persistence.mysql.entities.InventoryItemEntity;
import application.adapters.out.persistence.mysql.entities.OrderEntity;
import application.adapters.out.persistence.mysql.entities.ProductEntity;
import application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import application.adapters.out.persistence.mysql.entities.ReturnRequestEntity;
import application.adapters.out.persistence.mysql.entities.SellerEntity;
import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import application.domain.models.Buyer;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts domain relationships into JPA references (foreign keys) without loading the
 * referenced rows. The domain services have already validated that the referenced entities exist.
 */
@Component
public class JpaReferences {

    @PersistenceContext
    private EntityManager entityManager;

    public static String newIdentifier() {
        return UUID.randomUUID().toString();
    }

    public BuyerEntity buyer(Buyer buyer) {
        return buyer == null || buyer.getIdentification() == null ? null
                : entityManager.getReference(BuyerEntity.class, buyer.getIdentification());
    }

    public SellerEntity seller(Seller seller) {
        return seller == null || seller.getIdentification() == null ? null
                : entityManager.getReference(SellerEntity.class, seller.getIdentification());
    }

    public UserEntity user(User user) {
        return user == null || user.getUserId() == null ? null
                : entityManager.getReference(UserEntity.class, user.getUserId());
    }

    public WarehouseEntity warehouse(Warehouse warehouse) {
        return warehouse == null || warehouse.getIdentifier() == null ? null
                : entityManager.getReference(WarehouseEntity.class, warehouse.getIdentifier());
    }

    public ProductEntity product(Product product) {
        return product == null || product.getIdentifier() == null ? null
                : entityManager.getReference(ProductEntity.class, product.getIdentifier());
    }

    public ProductVariantEntity variant(ProductVariant variant) {
        return variant == null || variant.getIdentifier() == null ? null
                : entityManager.getReference(ProductVariantEntity.class, variant.getIdentifier());
    }

    public InventoryItemEntity inventoryItem(InventoryItem item) {
        return item == null || item.getIdentifier() == null ? null
                : entityManager.getReference(InventoryItemEntity.class, item.getIdentifier());
    }

    public OrderEntity order(Order order) {
        return order == null || order.getIdentifier() == null ? null
                : entityManager.getReference(OrderEntity.class, order.getIdentifier());
    }

    public ReturnRequestEntity returnRequest(ReturnRequest returnRequest) {
        return returnRequest == null || returnRequest.getIdentifier() == null ? null
                : entityManager.getReference(ReturnRequestEntity.class, returnRequest.getIdentifier());
    }
}
