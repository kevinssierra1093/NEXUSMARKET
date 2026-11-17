package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.OperationEntity;
import application.adapters.shared.TraceableEntityReferences;
import application.domain.models.Operation;
import application.domain.valueobjects.OperationType;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

public final class OperationPersistenceMapper {

    private OperationPersistenceMapper() {
    }

    public static Operation toDomain(OperationEntity entity) {
        if (entity == null) {
            return null;
        }
        Operation operation = new Operation();
        operation.setOperationId(entity.getOperationId());
        operation.setOperationType(fromCode(OperationType.class, entity.getOperationType()));
        operation.setExecutionDate(entity.getExecutionDate());
        operation.setPerformedBy(ParticipantPersistenceMapper.toDomain(entity.getPerformedBy()));
        operation.setAffectedEntity(TraceableEntityReferences.reference(
                entity.getAffectedEntityType(), entity.getAffectedEntityIdentifier()));
        return operation;
    }

    /** Scalar attributes; performedBy is resolved by OperationMySqlAdapter. */
    public static void copy(Operation operation, OperationEntity entity) {
        entity.setOperationType(code(operation.getOperationType()));
        entity.setExecutionDate(operation.getExecutionDate());
        entity.setAffectedEntityType(TraceableEntityReferences.typeOf(operation.getAffectedEntity()));
        entity.setAffectedEntityIdentifier(TraceableEntityReferences.identifierOf(operation.getAffectedEntity()));
    }
}
