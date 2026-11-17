package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultAuditLogsService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public List<AuditLog> executeByUser(User requestingUser, User targetUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return auditLogRepositoryPort.findByUser(targetUser);
    }

    public List<AuditLog> executeByEntity(User requestingUser, TraceableEntity entity) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return auditLogRepositoryPort.findByAffectedEntity(entity);
    }

    public List<AuditLog> executeByOperationType(User requestingUser, AuditLog auditLog) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return auditLogRepositoryPort.findByOperationType(auditLog);
    }
}
