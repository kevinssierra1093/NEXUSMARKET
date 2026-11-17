package application.domain.models;

import application.domain.valueobjects.WarehouseStatus;
import application.domain.valueobjects.WarehouseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Warehouse extends TraceableEntity {
    private String name;
    private String address;
    private WarehouseType type;
    // Null when type is MARKETPLACE.
    private Seller owner;
    private WarehouseStatus status;
}
