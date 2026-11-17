package application.domain.services.invoice;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InvoiceStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Internal domain service invoked when a refund of the whole order is processed. */
@Service
@RequiredArgsConstructor
public class AnnulInvoiceService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Invoice execute(User requestingUser, Order order) {
        Invoice invoice = invoiceRepositoryPort.findByOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Invoice"));
        if (!InvoiceStatus.ISSUED.equals(invoice.getStatus())) {
            throw new InvalidStatusTransitionException(invoice.getStatus().getCode(), InvoiceStatus.ANNULLED.getCode());
        }
        invoice.setStatus(InvoiceStatus.ANNULLED);
        invoiceRepositoryPort.update(invoice);
        Map<String, Object> details = new HashMap<>();
        details.put("order", order.getIdentifier());
        details.put("previousStatus", InvoiceStatus.ISSUED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.INVOICE_ANNULLED, invoice, details);
        return invoice;
    }
}
