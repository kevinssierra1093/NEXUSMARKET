package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ReturnRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequestEntity, String> {

    List<ReturnRequestEntity> findByOrder_Identifier(String orderIdentifier);

    List<ReturnRequestEntity> findByBuyer_IdentificationOrderByRequestDateDesc(String buyerIdentification);

    List<ReturnRequestEntity> findByStatusOrderByRequestDateAsc(String status);
}
