package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentEntity, String> {

    List<ShipmentEntity> findByOrder_Identifier(String orderIdentifier);
}
