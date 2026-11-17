package application.domain.services.operation;

import application.domain.models.AuditLog;
import application.domain.models.Operation;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterOperationAndAuditService {

    private final RegisterOperationService registerOperationService;
    private final RegisterAuditLogService registerAuditLogService;

    public void execute(Operation operation, Map<String, Object> auditDetails) {
        registerOperationService.execute(operation);

        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(operation.getOperationType());
        auditLog.setOperationDate(LocalDateTime.now());
        auditLog.setPerformedBy(operation.getPerformedBy());
        auditLog.setUserRole(operation.getPerformedBy().getRole());
        auditLog.setAffectedEntity(operation.getAffectedEntity());
        auditLog.setDetails(auditDetails);

        registerAuditLogService.execute(auditLog);
    }

    // Convenience overload: builds the Operation and delegates to the method above.
    public void execute(User user, OperationType type, TraceableEntity affectedEntity,
                        Map<String, Object> auditDetails) {
        Operation operation = new Operation();
        operation.setOperationType(type);
        operation.setExecutionDate(LocalDateTime.now());
        operation.setPerformedBy(user);
        operation.setAffectedEntity(affectedEntity);
        execute(operation, auditDetails);
    }
}
