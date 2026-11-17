package application.domain.valueobjects;

public final class RefundStatus extends DomainCatalog {

    public static final RefundStatus REQUESTED = new RefundStatus(
            "REQUESTED", "Requested", "Refund requested as a result of a return.");
    public static final RefundStatus APPROVED = new RefundStatus(
            "APPROVED", "Approved", "Refund approved for processing.");
    public static final RefundStatus REJECTED = new RefundStatus(
            "REJECTED", "Rejected", "Refund request denied.");
    public static final RefundStatus PROCESSED = new RefundStatus(
            "PROCESSED", "Processed", "Refund amount has been returned to the buyer.");

    private RefundStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
