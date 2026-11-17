package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.InvoiceEntity;
import application.adapters.out.persistence.mysql.mappers.CommercialPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.InvoiceJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.ports.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class InvoiceMySqlAdapter implements InvoiceRepositoryPort {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final JpaReferences references;

    @Override
    public Invoice save(Invoice invoice) {
        if (invoice.getIdentifier() == null) {
            invoice.setIdentifier(JpaReferences.newIdentifier());
        }
        InvoiceEntity entity = new InvoiceEntity();
        entity.setIdentifier(invoice.getIdentifier());
        entity.setOrder(references.order(invoice.getOrder()));
        CommercialPersistenceMapper.copy(invoice, entity);
        invoiceJpaRepository.save(entity);
        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findByIdentifier(Invoice invoice) {
        if (invoice == null || invoice.getIdentifier() == null) {
            return Optional.empty();
        }
        return invoiceJpaRepository.findById(invoice.getIdentifier()).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findByOrder(Order order) {
        if (order == null || order.getIdentifier() == null) {
            return Optional.empty();
        }
        return invoiceJpaRepository.findByOrder_Identifier(order.getIdentifier()).map(CommercialPersistenceMapper::toDomain);
    }

    @Override
    public void update(Invoice invoice) {
        InvoiceEntity entity = invoiceJpaRepository.findById(invoice.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Invoice"));
        CommercialPersistenceMapper.copy(invoice, entity);
        invoiceJpaRepository.save(entity);
    }
}
