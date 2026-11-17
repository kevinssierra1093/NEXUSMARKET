package application.domain.valueobjects;

public final class ProductType extends DomainCatalog {

    public static final ProductType PHYSICAL = new ProductType(
            "PHYSICAL", "Physical", "Tangible product requiring inventory and shipment.");
    public static final ProductType DIGITAL = new ProductType(
            "DIGITAL", "Digital", "Intangible product delivered immediately upon payment.");

    private ProductType(String code, String name, String description) {
        super(code, name, description);
    }
}
