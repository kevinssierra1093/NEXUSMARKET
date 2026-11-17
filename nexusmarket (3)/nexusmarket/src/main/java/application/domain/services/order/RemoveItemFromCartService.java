package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RemoveItemFromCartService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ResolveBuyerCartService resolveBuyerCartService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, ProductVariant variant) {
        Order cart = resolveBuyerCartService.find(requestingUser);
        boolean removed = cart.getItems()
                .removeIf(item -> item.getVariant().getIdentifier().equals(variant.getIdentifier()));
        if (!removed) {
            throw new EntityNotFoundException("Cart item");
        }
        orderRepositoryPort.update(cart);
        Map<String, Object> details = new HashMap<>();
        details.put("action", "REMOVE_ITEM");
        details.put("variant", variant.getIdentifier());
        registerOperationAndAuditService.execute(requestingUser, OperationType.CART_UPDATE, cart, details);
        return cart;
    }
}
