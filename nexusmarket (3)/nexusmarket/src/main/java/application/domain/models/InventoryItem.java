package application.domain.models;

import application.domain.valueobjects.InventoryItemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryItem extends TraceableEntity {
    private ProductVariant variant;
    private Warehouse warehouse;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private InventoryItemStatus status;
}
