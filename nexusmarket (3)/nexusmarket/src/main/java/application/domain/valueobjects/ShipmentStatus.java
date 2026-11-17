package application.domain.valueobjects;

public final class ShipmentStatus extends DomainCatalog {

    public static final ShipmentStatus PREPARING = new ShipmentStatus(
            "PREPARING", "Preparing", "Order is being packed at the origin warehouse.");
    public static final ShipmentStatus DISPATCHED = new ShipmentStatus(
            "DISPATCHED", "Dispatched", "Shipment has left the warehouse.");
    public static final ShipmentStatus IN_TRANSIT = new ShipmentStatus(
            "IN_TRANSIT", "In Transit", "Shipment is in transport toward the buyer.");
    public static final ShipmentStatus DELIVERED = new ShipmentStatus(
            "DELIVERED", "Delivered", "Shipment was successfully delivered.");
    public static final ShipmentStatus FAILED = new ShipmentStatus(
            "FAILED", "Failed", "Delivery attempt failed and requires resolution.");

    private ShipmentStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
