package application.domain.services.shipment;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ShipmentStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Transport tracking: DISPATCHED -> IN_TRANSIT -> FAILED -> IN_TRANSIT (retry).
 * Dispatch is done by DispatchOrderService and delivery by ConfirmShipmentDeliveryService.
 */
@Service
@RequiredArgsConstructor
public class UpdateShipmentStatusService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Shipment execute(User requestingUser, Shipment shipment, ShipmentStatus newStatus) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);
        Shipment stored = shipmentRepositoryPort.findByIdentifier(shipment)
                .orElseThrow(() -> new EntityNotFoundException("Shipment"));
        if (!isAllowed(stored.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        shipmentRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.SHIPMENT_UPDATE, stored, details);
        return stored;
    }

    private boolean isAllowed(ShipmentStatus from, ShipmentStatus to) {
        return (ShipmentStatus.DISPATCHED.equals(from) && ShipmentStatus.IN_TRANSIT.equals(to))
                || (ShipmentStatus.IN_TRANSIT.equals(from) && ShipmentStatus.FAILED.equals(to))
                || (ShipmentStatus.FAILED.equals(from) && ShipmentStatus.IN_TRANSIT.equals(to));
    }
}
