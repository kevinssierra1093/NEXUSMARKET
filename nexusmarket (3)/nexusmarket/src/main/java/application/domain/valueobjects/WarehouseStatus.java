package application.domain.valueobjects;

public final class WarehouseStatus extends DomainCatalog {

    public static final WarehouseStatus ACTIVE = new WarehouseStatus(
            "ACTIVE", "Active", "Warehouse is operational and may hold and dispatch stock.");
    public static final WarehouseStatus INACTIVE = new WarehouseStatus(
            "INACTIVE", "Inactive", "Warehouse is temporarily out of operation.");

    private WarehouseStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
