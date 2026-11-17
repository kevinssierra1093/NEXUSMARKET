package application.domain.services.invoice;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeOrderConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultInvoiceService {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeOrderConsultationService authorizeOrderConsultationService;

    public Invoice execute(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        authorizeOrderConsultationService.execute(requestingUser, stored);
        return invoiceRepositoryPort.findByOrder(stored)
                .orElseThrow(() -> new EntityNotFoundException("Invoice"));
    }
}
