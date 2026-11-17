package application.domain.valueobjects;

public final class SellerStatus extends DomainCatalog {

    public static final SellerStatus ACTIVE = new SellerStatus(
            "ACTIVE", "Active", "Seller may publish and manage products normally.");
    public static final SellerStatus SUSPENDED = new SellerStatus(
            "SUSPENDED", "Suspended", "Seller ability to publish or sell is temporarily disabled.");
    public static final SellerStatus INACTIVE = new SellerStatus(
            "INACTIVE", "Inactive", "Seller relationship exists but is not currently operating.");

    private SellerStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
