package application.domain.services.order;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.inventory.CheckStockAvailabilityService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ResolveBuyerCartService resolveBuyerCartService;
    private final CheckStockAvailabilityService checkStockAvailabilityService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, ProductVariant variant, Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new DomainException("The quantity must be greater than zero; remove the item instead.");
        }
        Order cart = resolveBuyerCartService.find(requestingUser);
        OrderItem line = cart.getItems().stream()
                .filter(item -> item.getVariant().getIdentifier().equals(variant.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item"));
        checkStockAvailabilityService.execute(line.getVariant(), newQuantity);
        int previous = line.getQuantity();
        line.setQuantity(newQuantity);
        orderRepositoryPort.update(cart);
        Map<String, Object> details = new HashMap<>();
        details.put("action", "UPDATE_QUANTITY");
        details.put("variant", variant.getIdentifier());
        details.put("previousQuantity", previous);
        details.put("newQuantity", newQuantity);
        registerOperationAndAuditService.execute(requestingUser, OperationType.CART_UPDATE, cart, details);
        return cart;
    }
}
