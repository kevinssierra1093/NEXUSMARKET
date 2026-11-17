package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationJpaRepository extends JpaRepository<OperationEntity, Integer> {

    List<OperationEntity> findByPerformedBy_UserIdOrderByExecutionDateDesc(Integer userId);

    List<OperationEntity> findByAffectedEntityTypeAndAffectedEntityIdentifierOrderByExecutionDateAsc(
            String affectedEntityType, String affectedEntityIdentifier);

    List<OperationEntity> findByOperationTypeOrderByExecutionDateDesc(String operationType);
}
