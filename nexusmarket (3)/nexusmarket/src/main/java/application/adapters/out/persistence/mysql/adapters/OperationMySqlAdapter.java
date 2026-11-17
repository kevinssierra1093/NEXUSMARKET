package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.OperationEntity;
import application.adapters.out.persistence.mysql.mappers.OperationPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.OperationJpaRepository;
import application.adapters.shared.TraceableEntityReferences;
import application.domain.models.Operation;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static application.adapters.shared.ValueObjectResolver.code;

/** Operations are immutable: there is no update. */
@Component
@Transactional
@RequiredArgsConstructor
public class OperationMySqlAdapter implements OperationRepositoryPort {

    private final OperationJpaRepository operationJpaRepository;
    private final JpaReferences references;

    @Override
    public Operation save(Operation operation) {
        OperationEntity entity = new OperationEntity();
        OperationPersistenceMapper.copy(operation, entity);
        entity.setPerformedBy(references.user(operation.getPerformedBy()));
        OperationEntity saved = operationJpaRepository.save(entity);
        operation.setOperationId(saved.getOperationId());
        return operation;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Operation> findById(Operation operation) {
        if (operation == null || operation.getOperationId() == null) {
            return Optional.empty();
        }
        return operationJpaRepository.findById(operation.getOperationId()).map(OperationPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return operationJpaRepository.findByPerformedBy_UserIdOrderByExecutionDateDesc(user.getUserId()).stream()
                .map(OperationPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findByAffectedEntity(TraceableEntity entity) {
        if (entity == null || entity.getIdentifier() == null) {
            return List.of();
        }
        return operationJpaRepository.findByAffectedEntityTypeAndAffectedEntityIdentifierOrderByExecutionDateAsc(
                        TraceableEntityReferences.typeOf(entity), entity.getIdentifier()).stream()
                .map(OperationPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> findByType(Operation operation) {
        return operationJpaRepository.findByOperationTypeOrderByExecutionDateDesc(code(operation.getOperationType()))
                .stream().map(OperationPersistenceMapper::toDomain).toList();
    }
}
