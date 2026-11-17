package application.domain.services.returns;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.inventory.RegisterInventoryReturnService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ReturnStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A logistics operator receives the returned product: ONLY the units of the returned line
 * re-enter the warehouse they were dispatched from. The other lines are not touched.
 */
@Service
@RequiredArgsConstructor
public class ConfirmReturnReceptionService {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ResolveReturnLineService resolveReturnLineService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterInventoryReturnService registerInventoryReturnService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public ReturnRequest execute(User requestingUser, ReturnRequest request) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.LOGISTICS_OPERATOR);

        ReturnRequest stored = returnRequestRepositoryPort.findByIdentifier(request)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        if (!ReturnStatus.APPROVED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), ReturnStatus.RECEIVED.getCode());
        }
        Order order = orderRepositoryPort.findByIdentifier(stored.getOrder())
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        OrderItem line = resolveReturnLineService.execute(order, stored.getVariant());
        if (line.getWarehouse() == null) {
            throw new DomainException("Digital products have no goods to receive; request the refund directly.");
        }
        registerInventoryReturnService.execute(requestingUser, line, order);

        stored.setStatus(ReturnStatus.RECEIVED);
        returnRequestRepositoryPort.update(stored);

        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", ReturnStatus.APPROVED.getCode());
        details.put("newStatus", ReturnStatus.RECEIVED.getCode());
        details.put("variant", line.getVariant().getIdentifier());
        details.put("quantity", line.getQuantity());
        details.put("warehouse", line.getWarehouse().getIdentifier());
        registerOperationAndAuditService.execute(requestingUser, OperationType.RETURN_RECEPTION, stored, details);
        return stored;
    }
}
