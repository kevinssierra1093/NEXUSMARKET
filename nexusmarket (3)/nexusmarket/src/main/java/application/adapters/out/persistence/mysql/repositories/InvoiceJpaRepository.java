package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, String> {

    Optional<InvoiceEntity> findByOrder_Identifier(String orderIdentifier);
}
