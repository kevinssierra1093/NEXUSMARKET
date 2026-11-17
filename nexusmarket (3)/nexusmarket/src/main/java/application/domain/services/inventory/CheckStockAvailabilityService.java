package application.domain.services.inventory;

import application.domain.exceptions.DomainException;
import application.domain.models.ProductVariant;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Internal domain service invoked by the cart services (already authorized). Verifies that a
 * physical variant has enough sellable stock BEFORE it is accepted in the cart.
 *
 * It applies the same rule as ReserveInventoryService (one AVAILABLE record in an ACTIVE
 * warehouse must cover the whole quantity), so a cart accepted here can be reserved later,
 * unless other buyers consume the stock first. Digital products have no inventory.
 */
@Service
@RequiredArgsConstructor
public class CheckStockAvailabilityService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;

    /** Units of the variant that can be sold in a single line (max available in one warehouse). */
    public int availableUnits(ProductVariant variant) {
        return inventoryItemRepositoryPort.findByVariant(variant).stream()
                .filter(item -> InventoryItemStatus.AVAILABLE.equals(item.getStatus()))
                .filter(item -> WarehouseStatus.ACTIVE.equals(item.getWarehouse().getStatus()))
                .mapToInt(item -> item.getQuantityAvailable())
                .max()
                .orElse(0);
    }

    public void execute(ProductVariant variant, int requestedQuantity) {
        if (!ProductType.PHYSICAL.equals(variant.getProduct().getType())) {
            return;
        }
        int available = availableUnits(variant);
        if (requestedQuantity > available) {
            throw new DomainException("Insufficient stock for '" + variant.getProduct().getName() + "': requested "
                    + requestedQuantity + " unit(s), available " + available + ".");
        }
    }
}
