package application.domain.services.order;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.inventory.ReserveInventoryService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.BuyerStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * CART -> PENDING_PAYMENT. Physical items reserve stock; digital items need no inventory.
 * Must run inside a single transaction in the adapter layer so that a failed reservation
 * leaves no partial reservations behind.
 */
@Service
@RequiredArgsConstructor
public class ConfirmOrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final ReserveInventoryService reserveInventoryService;
    private final CalculateOrderTotalService calculateOrderTotalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateBuyerOwnershipService.execute(requestingUser, stored.getBuyer());
        if (!OrderStatus.CART.equals(stored.getOrderStatus())) {
            throw new InvalidStatusTransitionException(stored.getOrderStatus().getCode(),
                    OrderStatus.PENDING_PAYMENT.getCode());
        }
        validateConfirmable(stored);
        for (OrderItem line : stored.getItems()) {
            if (ProductType.PHYSICAL.equals(line.getVariant().getProduct().getType())) {
                InventoryItem reserved = reserveInventoryService.execute(
                        requestingUser, line.getVariant(), line.getQuantity(), stored);
                line.setWarehouse(reserved.getWarehouse());
            }
        }
        stored.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        orderRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", OrderStatus.CART.getCode());
        details.put("newStatus", OrderStatus.PENDING_PAYMENT.getCode());
        details.put("items", stored.getItems().size());
        details.put("total", calculateOrderTotalService.execute(stored));
        registerOperationAndAuditService.execute(requestingUser, OperationType.ORDER_CONFIRMATION, stored, details);
        return stored;
    }

    private void validateConfirmable(Order order) {
        if (!BuyerStatus.ACTIVE.equals(order.getBuyer().getStatus())) {
            throw new DomainException("The buyer is not enabled to make purchases.");
        }
        if (order.getItems().isEmpty()) {
            throw new DomainException("An empty cart cannot be confirmed.");
        }
        for (OrderItem line : order.getItems()) {
            if (!ProductStatus.PUBLISHED.equals(line.getVariant().getProduct().getStatus())) {
                throw new DomainException("The product " + line.getVariant().getProduct().getName()
                        + " is no longer available.");
            }
        }
    }
}
