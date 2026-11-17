package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, String> {

    List<WarehouseEntity> findByOwner_Identification(String sellerIdentification);
}
