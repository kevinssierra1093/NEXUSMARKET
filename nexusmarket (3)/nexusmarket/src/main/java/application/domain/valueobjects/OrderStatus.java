package application.domain.valueobjects;

public final class OrderStatus extends DomainCatalog {

    public static final OrderStatus CART = new OrderStatus(
            "CART", "Cart", "Provisional product selection; freely editable by the buyer.");
    public static final OrderStatus PENDING_PAYMENT = new OrderStatus(
            "PENDING_PAYMENT", "Pending Payment", "Order confirmed; awaiting financial confirmation.");
    public static final OrderStatus PAID = new OrderStatus(
            "PAID", "Paid", "Payment confirmed; preparation process may begin.");
    public static final OrderStatus SHIPPED = new OrderStatus(
            "SHIPPED", "Shipped", "Order has physically left the warehouse.");
    public static final OrderStatus FINALIZED = new OrderStatus(
            "FINALIZED", "Finalized", "Order successfully delivered and closed.");

    private OrderStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
