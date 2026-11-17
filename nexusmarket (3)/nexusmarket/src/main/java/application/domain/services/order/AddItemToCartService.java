package application.domain.services.order;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.services.inventory.CheckStockAvailabilityService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddItemToCartService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final ResolveBuyerCartService resolveBuyerCartService;
    private final CheckStockAvailabilityService checkStockAvailabilityService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, ProductVariant variant, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("The quantity must be greater than zero.");
        }
        Order cart = resolveBuyerCartService.findOrCreate(requestingUser);
        ProductVariant stored = productVariantRepositoryPort.findByIdentifier(variant)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant"));
        if (!ProductStatus.PUBLISHED.equals(stored.getProduct().getStatus())) {
            throw new DomainException("Only published products can be added to the cart.");
        }
        OrderItem line = cart.getItems().stream()
                .filter(item -> item.getVariant().getIdentifier().equals(stored.getIdentifier()))
                .findFirst().orElse(null);
        // Physical products: the total quantity in the cart cannot exceed the available stock.
        int totalQuantity = (line == null ? 0 : line.getQuantity()) + quantity;
        checkStockAvailabilityService.execute(stored, totalQuantity);
        if (line == null) {
            line = new OrderItem();
            line.setOrder(cart);
            line.setVariant(stored);
            line.setQuantity(quantity);
            line.setUnitPrice(stored.getPrice());
            cart.getItems().add(line);
        } else {
            line.setQuantity(line.getQuantity() + quantity);
        }
        orderRepositoryPort.update(cart);
        Map<String, Object> details = new HashMap<>();
        details.put("action", "ADD_ITEM");
        details.put("variant", stored.getIdentifier());
        details.put("quantity", quantity);
        registerOperationAndAuditService.execute(requestingUser, OperationType.CART_UPDATE, cart, details);
        return cart;
    }
}
