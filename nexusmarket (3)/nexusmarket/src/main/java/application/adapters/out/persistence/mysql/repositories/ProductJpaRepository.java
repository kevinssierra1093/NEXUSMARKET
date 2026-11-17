package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findBySeller_Identification(String sellerIdentification);

    List<ProductEntity> findByStatus(String status);
}
