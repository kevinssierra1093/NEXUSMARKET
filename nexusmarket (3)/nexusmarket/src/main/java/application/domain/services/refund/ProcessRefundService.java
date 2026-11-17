package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.invoice.AnnulInvoiceService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.RefundStatus;
import application.domain.valueobjects.ReturnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * APPROVED -> PROCESSED: the amount of the returned product is reimbursed and its return is
 * closed (COMPLETED). The invoice is annulled only when EVERY product of the order has been
 * returned and refunded; with a partial return the invoice stays ISSUED and the other
 * products keep their state.
 */
@Service
@RequiredArgsConstructor
public class ProcessRefundService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final AnnulInvoiceService annulInvoiceService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Refund execute(User requestingUser, Refund refund) {
        authorizeAdministrationService.execute(requestingUser);

        Refund stored = refundRepositoryPort.findByIdentifier(refund)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
        if (!RefundStatus.APPROVED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), RefundStatus.PROCESSED.getCode());
        }
        ReturnRequest returnRequest = returnRequestRepositoryPort.findByIdentifier(stored.getReturnRequest())
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        Order order = orderRepositoryPort.findByIdentifier(returnRequest.getOrder())
                .orElseThrow(() -> new EntityNotFoundException("Order"));

        stored.setStatus(RefundStatus.PROCESSED);
        stored.setProcessedDate(LocalDateTime.now());
        refundRepositoryPort.update(stored);
        returnRequest.setStatus(ReturnStatus.COMPLETED);
        returnRequestRepositoryPort.update(returnRequest);

        boolean wholeOrderRefunded = allLinesReturned(order, returnRequest);
        if (wholeOrderRefunded) {
            annulInvoiceService.execute(requestingUser, order);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", RefundStatus.APPROVED.getCode());
        details.put("newStatus", RefundStatus.PROCESSED.getCode());
        details.put("amount", stored.getAmount());
        details.put("variant", returnRequest.getVariant().getIdentifier());
        details.put("invoiceAnnulled", wholeOrderRefunded);
        registerOperationAndAuditService.execute(requestingUser, OperationType.REFUND_PROCESSING, stored, details);
        return stored;
    }

    private boolean allLinesReturned(Order order, ReturnRequest current) {
        Set<String> completed = returnRequestRepositoryPort.findByOrder(order).stream()
                .filter(r -> ReturnStatus.COMPLETED.equals(r.getStatus()) && r.getVariant() != null)
                .map(r -> r.getVariant().getIdentifier())
                .collect(Collectors.toSet());
        completed.add(current.getVariant().getIdentifier());
        return order.getItems().stream().allMatch(line -> completed.contains(line.getVariant().getIdentifier()));
    }
}
