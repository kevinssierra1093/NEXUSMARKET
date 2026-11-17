package application.domain.models;

import application.domain.valueobjects.InventoryMovementType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryMovement {
    private Integer movementId;
    private InventoryItem inventoryItem;
    private InventoryMovementType movementType;
    private Integer quantity;
    private LocalDateTime movementDate;
    private User performedBy;
    // Order that originated the movement, when applicable.
    private Order relatedOrder;
}
