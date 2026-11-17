package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditLogResponse(
        String auditId,
        String operationType,
        LocalDateTime operationDate,
        Integer performedByUserId,
        String performedByUsername,
        String userRole,
        String affectedEntityType,
        String affectedEntityIdentifier,
        Map<String, Object> details) {
}
