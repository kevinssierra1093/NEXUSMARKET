package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Integer movementId,
        String inventoryItemIdentifier,
        String movementType,
        Integer quantity,
        LocalDateTime movementDate,
        String performedBy,
        String relatedOrderIdentifier) {
}
