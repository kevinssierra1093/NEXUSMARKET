package application.domain.ports.out;

import application.domain.models.InventoryItem;
import application.domain.models.ProductVariant;
import application.domain.models.Warehouse;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepositoryPort {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findByIdentifier(InventoryItem item);

    Optional<InventoryItem> findByVariantAndWarehouse(InventoryItem item);

    List<InventoryItem> findByVariant(ProductVariant variant);

    List<InventoryItem> findByWarehouse(Warehouse warehouse);

    List<InventoryItem> findAll();

    void update(InventoryItem item);
}
