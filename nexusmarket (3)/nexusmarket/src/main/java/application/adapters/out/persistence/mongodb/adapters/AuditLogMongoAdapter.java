package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.out.persistence.mongodb.mappers.AuditLogDocumentMapper;
import application.adapters.out.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.adapters.shared.TraceableEntityReferences;
import application.domain.models.AuditLog;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.code;

/**
 * Audit logs live in MongoDB, outside the MySQL transaction of the use case: if the business
 * transaction rolls back, the audit entry of the attempted action remains.
 */
@Component
@RequiredArgsConstructor
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository auditLogMongoRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogDocument saved = auditLogMongoRepository.save(AuditLogDocumentMapper.toDocument(auditLog));
        auditLog.setAuditId(saved.getId());
        return auditLog;
    }

    @Override
    public List<AuditLog> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            return List.of();
        }
        return auditLogMongoRepository.findByPerformedBy_UserIdOrderByOperationDateDesc(user.getUserId()).stream()
                .map(AuditLogDocumentMapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByAffectedEntity(TraceableEntity entity) {
        if (entity == null || entity.getIdentifier() == null) {
            return List.of();
        }
        return auditLogMongoRepository.findByAffectedEntity_TypeAndAffectedEntity_IdentifierOrderByOperationDateAsc(
                        TraceableEntityReferences.typeOf(entity), entity.getIdentifier()).stream()
                .map(AuditLogDocumentMapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByOperationType(AuditLog auditLog) {
        return auditLogMongoRepository.findByOperationTypeOrderByOperationDateDesc(code(auditLog.getOperationType()))
                .stream().map(AuditLogDocumentMapper::toDomain).toList();
    }
}
