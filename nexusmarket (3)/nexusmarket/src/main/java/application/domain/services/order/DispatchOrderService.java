package application.domain.services.order;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.inventory.RegisterSaleOutboundService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ShipmentStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PAID -> SHIPPED. A logistics operator dispatches every prepared shipment of the order and
 * the reserved stock leaves the warehouses (SALE_OUTBOUND).
 */
@Service
@RequiredArgsConstructor
public class DispatchOrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterSaleOutboundService registerSaleOutboundService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, Order order) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        if (!OrderStatus.PAID.equals(stored.getOrderStatus())) {
            throw new InvalidStatusTransitionException(stored.getOrderStatus().getCode(), OrderStatus.SHIPPED.getCode());
        }
        List<Shipment> pending = shipmentRepositoryPort.findByOrder(stored).stream()
                .filter(shipment -> ShipmentStatus.PREPARING.equals(shipment.getStatus()))
                .toList();
        if (pending.isEmpty()) {
            throw new DomainException("The order has no shipments pending dispatch.");
        }
        LocalDateTime now = LocalDateTime.now();
        for (Shipment shipment : pending) {
            shipment.setHandledBy(requestingUser);
            shipment.setStatus(ShipmentStatus.DISPATCHED);
            shipment.setDispatchDate(now);
            shipmentRepositoryPort.update(shipment);
        }
        for (OrderItem line : stored.getItems()) {
            if (line.getWarehouse() != null) {
                registerSaleOutboundService.execute(requestingUser, line, stored);
            }
        }
        stored.setOrderStatus(OrderStatus.SHIPPED);
        stored.setDispatchDate(now);
        orderRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", OrderStatus.PAID.getCode());
        details.put("newStatus", OrderStatus.SHIPPED.getCode());
        details.put("shipments", pending.size());
        registerOperationAndAuditService.execute(requestingUser, OperationType.ORDER_DISPATCH, stored, details);
        return stored;
    }
}
