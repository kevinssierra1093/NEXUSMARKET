package application.domain.valueobjects;

public final class BuyerStatus extends DomainCatalog {

    public static final BuyerStatus ACTIVE = new BuyerStatus(
            "ACTIVE", "Active", "Buyer may place orders normally.");
    public static final BuyerStatus RESTRICTED = new BuyerStatus(
            "RESTRICTED", "Restricted", "Buyer may browse but has limited purchasing capability.");
    public static final BuyerStatus BLOCKED = new BuyerStatus(
            "BLOCKED", "Blocked", "Buyer is not permitted to place orders.");

    private BuyerStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
