package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.AuditLogResponse;
import application.adapters.in.rest.responses.OperationResponse;
import application.adapters.shared.TraceableEntityReferences;
import application.domain.models.AuditLog;
import application.domain.models.Operation;

import java.util.Map;

import static application.adapters.shared.ValueObjectResolver.code;

public final class AuditRestMapper {

    private AuditRestMapper() {
    }

    public static OperationResponse toResponse(Operation operation) {
        return new OperationResponse(
                operation.getOperationId(),
                code(operation.getOperationType()),
                operation.getExecutionDate(),
                operation.getPerformedBy() == null ? null : operation.getPerformedBy().getUserId(),
                operation.getPerformedBy() == null ? null : operation.getPerformedBy().getUsername(),
                TraceableEntityReferences.typeOf(operation.getAffectedEntity()),
                TraceableEntityReferences.identifierOf(operation.getAffectedEntity()));
    }

    public static AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getAuditId(),
                code(auditLog.getOperationType()),
                auditLog.getOperationDate(),
                auditLog.getPerformedBy() == null ? null : auditLog.getPerformedBy().getUserId(),
                auditLog.getPerformedBy() == null ? null : auditLog.getPerformedBy().getUsername(),
                code(auditLog.getUserRole()),
                TraceableEntityReferences.typeOf(auditLog.getAffectedEntity()),
                TraceableEntityReferences.identifierOf(auditLog.getAffectedEntity()),
                auditLog.getDetails() == null ? Map.of() : auditLog.getDetails());
    }
}
