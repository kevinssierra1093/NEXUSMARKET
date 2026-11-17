package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    Optional<OrderEntity> findFirstByBuyer_IdentificationAndOrderStatus(String buyerIdentification, String orderStatus);

    List<OrderEntity> findByBuyer_IdentificationOrderByCreationDateDesc(String buyerIdentification);

    List<OrderEntity> findByOrderStatusOrderByCreationDateAsc(String orderStatus);
}
