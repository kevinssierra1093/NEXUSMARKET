package application.domain.valueobjects;

public final class InventoryItemStatus extends DomainCatalog {

    public static final InventoryItemStatus AVAILABLE = new InventoryItemStatus(
            "AVAILABLE", "Available", "Stock is in good condition and may be reserved or sold.");
    public static final InventoryItemStatus DAMAGED = new InventoryItemStatus(
            "DAMAGED", "Damaged", "Stock is damaged and can never be reserved or sold.");

    private InventoryItemStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
