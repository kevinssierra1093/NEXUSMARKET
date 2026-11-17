package application.domain.ports.out;

import application.domain.models.AuditLog;
import application.domain.models.TraceableEntity;
import application.domain.models.User;

import java.util.List;

public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByAffectedEntity(TraceableEntity entity);

    List<AuditLog> findByOperationType(AuditLog auditLog);
}
