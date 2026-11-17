package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundJpaRepository extends JpaRepository<RefundEntity, String> {

    Optional<RefundEntity> findByReturnRequest_Identifier(String returnRequestIdentifier);
}
