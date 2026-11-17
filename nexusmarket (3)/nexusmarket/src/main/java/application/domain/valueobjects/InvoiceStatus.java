package application.domain.valueobjects;

public final class InvoiceStatus extends DomainCatalog {

    public static final InvoiceStatus ISSUED = new InvoiceStatus(
            "ISSUED", "Issued", "Invoice has been generated and is valid.");
    public static final InvoiceStatus ANNULLED = new InvoiceStatus(
            "ANNULLED", "Annulled", "Invoice has been cancelled.");

    private InvoiceStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
