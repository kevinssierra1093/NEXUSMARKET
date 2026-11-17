package application.domain.services.shipment;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.order.FinalizeOrderService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ShipmentStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Confirms a delivery. When every shipment of the order has been delivered, the order is
 * finalized (business flow, step 8 "Cierre").
 */
@Service
@RequiredArgsConstructor
public class ConfirmShipmentDeliveryService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final FinalizeOrderService finalizeOrderService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Shipment execute(User requestingUser, Shipment shipment) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Shipment stored = shipmentRepositoryPort.findByIdentifier(shipment)
                .orElseThrow(() -> new EntityNotFoundException("Shipment"));
        boolean deliverable = ShipmentStatus.DISPATCHED.equals(stored.getStatus())
                || ShipmentStatus.IN_TRANSIT.equals(stored.getStatus());
        if (!deliverable) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(),
                    ShipmentStatus.DELIVERED.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(ShipmentStatus.DELIVERED);
        stored.setDeliveryDate(LocalDateTime.now());
        shipmentRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", ShipmentStatus.DELIVERED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.SHIPMENT_UPDATE, stored, details);
        finalizeOrderIfCompleted(requestingUser, stored);
        return stored;
    }

    private void finalizeOrderIfCompleted(User requestingUser, Shipment delivered) {
        Order order = orderRepositoryPort.findByIdentifier(delivered.getOrder())
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        boolean allDelivered = shipmentRepositoryPort.findByOrder(order).stream()
                .allMatch(s -> ShipmentStatus.DELIVERED.equals(s.getStatus()));
        if (allDelivered && OrderStatus.SHIPPED.equals(order.getOrderStatus())) {
            finalizeOrderService.execute(requestingUser, order);
        }
    }
}
