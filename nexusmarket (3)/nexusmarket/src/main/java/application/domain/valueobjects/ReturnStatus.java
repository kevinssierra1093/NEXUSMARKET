package application.domain.valueobjects;

public final class ReturnStatus extends DomainCatalog {

    public static final ReturnStatus REQUESTED = new ReturnStatus(
            "REQUESTED", "Requested", "Return request submitted by the buyer.");
    public static final ReturnStatus APPROVED = new ReturnStatus(
            "APPROVED", "Approved", "Return request approved; awaiting receipt of goods.");
    public static final ReturnStatus REJECTED = new ReturnStatus(
            "REJECTED", "Rejected", "Return request denied.");
    public static final ReturnStatus RECEIVED = new ReturnStatus(
            "RECEIVED", "Received", "Returned goods received at the warehouse.");
    public static final ReturnStatus COMPLETED = new ReturnStatus(
            "COMPLETED", "Completed", "Return process closed after the refund was processed.");

    private ReturnStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
