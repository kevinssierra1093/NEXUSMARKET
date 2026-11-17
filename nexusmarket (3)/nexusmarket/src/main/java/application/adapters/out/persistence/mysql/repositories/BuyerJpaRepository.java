package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerJpaRepository extends JpaRepository<BuyerEntity, String> {

    boolean existsByEmail(String email);
}
