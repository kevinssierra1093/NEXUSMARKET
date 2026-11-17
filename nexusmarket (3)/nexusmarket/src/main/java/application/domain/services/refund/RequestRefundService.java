package application.domain.services.refund;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeRefundOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.returns.ResolveReturnLineService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.RefundStatus;
import application.domain.valueobjects.ReturnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Requests the reimbursement of a returned product. A physical product must have been
 * RECEIVED back in the warehouse; a digital product only needs the approved return.
 * The amount is the subtotal of the returned line (unit price x quantity), not the whole order.
 */
@Service
@RequiredArgsConstructor
public class RequestRefundService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ResolveReturnLineService resolveReturnLineService;
    private final AuthorizeRefundOperationService authorizeRefundOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Refund execute(User requestingUser, ReturnRequest returnRequest) {
        ReturnRequest stored = returnRequestRepositoryPort.findByIdentifier(returnRequest)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        authorizeRefundOperationService.execute(requestingUser, stored.getBuyer());

        Order order = orderRepositoryPort.findByIdentifier(stored.getOrder())
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        OrderItem line = resolveReturnLineService.execute(order, stored.getVariant());
        validateReady(stored, line);
        if (refundRepositoryPort.findByReturnRequest(stored).isPresent()) {
            throw new DomainException("The return request already has a refund.");
        }

        Refund refund = new Refund();
        refund.setReturnRequest(stored);
        refund.setAmount(line.getUnitPrice().multiply(BigDecimal.valueOf(stored.getQuantity())));
        refund.setStatus(RefundStatus.REQUESTED);
        Refund saved = refundRepositoryPort.save(refund);

        Map<String, Object> details = new HashMap<>();
        details.put("returnRequest", stored.getIdentifier());
        details.put("variant", line.getVariant().getIdentifier());
        details.put("amount", saved.getAmount());
        registerOperationAndAuditService.execute(requestingUser, OperationType.REFUND_REQUEST, saved, details);
        return saved;
    }

    private void validateReady(ReturnRequest returnRequest, OrderItem line) {
        boolean physical = line.getWarehouse() != null;
        boolean ready = ReturnStatus.RECEIVED.equals(returnRequest.getStatus())
                || (ReturnStatus.APPROVED.equals(returnRequest.getStatus()) && !physical);
        if (!ready) {
            throw new DomainException("A refund can only be requested after the returned product was received "
                    + "(or after approval for digital products).");
        }
    }
}
