package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.invoice.IssueInvoiceService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.shipment.CreateShipmentsService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * PENDING_PAYMENT -> PAID. Issues the invoice and starts the preparation flow (one shipment
 * per origin warehouse for physical items). Orders with only digital items are delivered
 * immediately after payment and finalized right away.
 */
@Service
@RequiredArgsConstructor
public class ConfirmOrderPaymentService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final IssueInvoiceService issueInvoiceService;
    private final CreateShipmentsService createShipmentsService;
    private final FinalizeOrderService finalizeOrderService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateBuyerOwnershipService.execute(requestingUser, stored.getBuyer());
        if (!OrderStatus.PENDING_PAYMENT.equals(stored.getOrderStatus())) {
            throw new InvalidStatusTransitionException(stored.getOrderStatus().getCode(), OrderStatus.PAID.getCode());
        }
        stored.setOrderStatus(OrderStatus.PAID);
        stored.setPaymentDate(LocalDateTime.now());
        orderRepositoryPort.update(stored);
        issueInvoiceService.execute(requestingUser, stored);

        boolean hasPhysicalItems = stored.getItems().stream()
                .anyMatch(line -> ProductType.PHYSICAL.equals(line.getVariant().getProduct().getType()));
        if (hasPhysicalItems) {
            createShipmentsService.execute(requestingUser, stored);
        }
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", OrderStatus.PENDING_PAYMENT.getCode());
        details.put("newStatus", OrderStatus.PAID.getCode());
        details.put("hasPhysicalItems", hasPhysicalItems);
        registerOperationAndAuditService.execute(requestingUser, OperationType.ORDER_PAYMENT, stored, details);
        if (!hasPhysicalItems) {
            finalizeOrderService.execute(requestingUser, stored);
        }
        return stored;
    }
}
