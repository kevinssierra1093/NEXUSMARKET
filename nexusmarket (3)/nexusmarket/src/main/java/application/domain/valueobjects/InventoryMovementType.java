package application.domain.valueobjects;

public final class InventoryMovementType extends DomainCatalog {

    public static final InventoryMovementType INBOUND = new InventoryMovementType(
            "INBOUND", "Inbound", "Stock entering the warehouse (initial load or restock).");
    public static final InventoryMovementType RESERVATION = new InventoryMovementType(
            "RESERVATION", "Reservation", "Stock temporarily reserved for a pending order.");
    public static final InventoryMovementType SALE_OUTBOUND = new InventoryMovementType(
            "SALE_OUTBOUND", "Sale Outbound", "Stock permanently removed due to a sale.");
    public static final InventoryMovementType ADJUSTMENT = new InventoryMovementType(
            "ADJUSTMENT", "Adjustment", "Manual correction of recorded stock levels.");
    public static final InventoryMovementType RETURN = new InventoryMovementType(
            "RETURN", "Return", "Stock re-entering the warehouse due to a buyer return.");

    private InventoryMovementType(String code, String name, String description) {
        super(code, name, description);
    }
}
