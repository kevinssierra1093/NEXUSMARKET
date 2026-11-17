package application.domain.ports.out;

import application.domain.models.Invoice;
import application.domain.models.Order;

import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByIdentifier(Invoice invoice);

    Optional<Invoice> findByOrder(Order order);

    void update(Invoice invoice);
}
