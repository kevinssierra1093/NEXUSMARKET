package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, String> {

    boolean existsByEmail(String email);
}
