package application.domain.models;

import application.domain.valueobjects.ShipmentStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Shipment extends TraceableEntity {
    private Order order;
    private Warehouse originWarehouse;
    // Logistics operator assigned when the shipment is dispatched; null while PREPARING.
    private User handledBy;
    private ShipmentStatus status;
    private LocalDateTime dispatchDate;
    private LocalDateTime deliveryDate;
}
