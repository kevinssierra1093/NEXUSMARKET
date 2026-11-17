package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record OperationResponse(
        Integer operationId,
        String operationType,
        LocalDateTime executionDate,
        Integer performedByUserId,
        String performedByUsername,
        String affectedEntityType,
        String affectedEntityIdentifier) {
}
