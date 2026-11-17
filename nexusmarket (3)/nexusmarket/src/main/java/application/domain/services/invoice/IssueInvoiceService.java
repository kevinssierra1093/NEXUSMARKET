package application.domain.services.invoice;

import application.domain.exceptions.DomainException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.services.order.CalculateOrderTotalService;
import application.domain.valueobjects.InvoiceStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** Internal domain service invoked when the payment of an order is confirmed. */
@Service
@RequiredArgsConstructor
public class IssueInvoiceService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final CalculateOrderTotalService calculateOrderTotalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Invoice execute(User requestingUser, Order order) {
        if (invoiceRepositoryPort.findByOrder(order).isPresent()) {
            throw new DomainException("The order " + order.getIdentifier() + " already has an invoice.");
        }
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setTotalAmount(calculateOrderTotalService.execute(order));
        invoice.setStatus(InvoiceStatus.ISSUED);
        Invoice saved = invoiceRepositoryPort.save(invoice);
        Map<String, Object> details = new HashMap<>();
        details.put("order", order.getIdentifier());
        details.put("totalAmount", saved.getTotalAmount());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVOICE_ISSUED, saved, details);
        return saved;
    }
}
