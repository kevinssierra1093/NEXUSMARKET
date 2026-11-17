package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantEntity, String> {

    List<ProductVariantEntity> findByProduct_Identifier(String productIdentifier);
}
