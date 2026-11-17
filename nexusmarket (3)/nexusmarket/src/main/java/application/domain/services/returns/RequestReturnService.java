package application.domain.services.returns;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ReturnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * The buyer requests the return of ONE product (line) of a finalized order. A finalized order
 * can never be modified, so the return is a separate request that refers to the order and to
 * the returned variant; the other lines keep their state.
 */
@Service
@RequiredArgsConstructor
public class RequestReturnService {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ResolveReturnLineService resolveReturnLineService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public ReturnRequest execute(User requestingUser, ReturnRequest request) {
        Order order = orderRepositoryPort.findByIdentifier(request.getOrder())
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateBuyerOwnershipService.execute(requestingUser, order.getBuyer());
        if (!OrderStatus.FINALIZED.equals(order.getOrderStatus())) {
            throw new DomainException("Only finalized orders can be returned.");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new DomainException("The reason of the return is required.");
        }
        OrderItem line = resolveReturnLineService.execute(order, request.getVariant());
        boolean alreadyRequested = returnRequestRepositoryPort.findByOrder(order).stream()
                .filter(existing -> existing.getVariant() != null)
                .filter(existing -> existing.getVariant().getIdentifier().equals(line.getVariant().getIdentifier()))
                .anyMatch(existing -> !ReturnStatus.REJECTED.equals(existing.getStatus()));
        if (alreadyRequested) {
            throw new DomainException("This product already has a return request in progress or completed.");
        }

        request.setOrder(order);
        request.setBuyer(order.getBuyer());
        request.setVariant(line.getVariant());
        request.setQuantity(line.getQuantity());
        request.setStatus(ReturnStatus.REQUESTED);
        request.setRequestDate(LocalDateTime.now());
        ReturnRequest saved = returnRequestRepositoryPort.save(request);

        Map<String, Object> details = new HashMap<>();
        details.put("order", order.getIdentifier());
        details.put("variant", line.getVariant().getIdentifier());
        details.put("product", line.getVariant().getProduct().getName());
        details.put("seller", line.getVariant().getProduct().getSeller().getIdentification());
        details.put("quantity", line.getQuantity());
        details.put("reason", saved.getReason());
        registerOperationAndAuditService.execute(requestingUser, OperationType.RETURN_REQUEST, saved, details);
        return saved;
    }
}
