package application.domain.valueobjects;

public final class WarehouseType extends DomainCatalog {

    public static final WarehouseType MARKETPLACE = new WarehouseType(
            "MARKETPLACE", "Marketplace", "Warehouse owned and operated by NexusMarket.");
    public static final WarehouseType SELLER = new WarehouseType(
            "SELLER", "Seller", "Warehouse owned and operated by a registered Seller.");

    private WarehouseType(String code, String name, String description) {
        super(code, name, description);
    }
}
