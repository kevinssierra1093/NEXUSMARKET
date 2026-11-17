package application.domain.valueobjects;

public final class OperationType extends DomainCatalog {

    public static final OperationType SELLER_REGISTRATION = new OperationType(
            "SELLER_REGISTRATION", "Seller Registration", "A seller was incorporated by an administrator.");
    public static final OperationType WAREHOUSE_REGISTRATION = new OperationType(
            "WAREHOUSE_REGISTRATION", "Warehouse Registration", "A warehouse was registered.");
    public static final OperationType WAREHOUSE_UPDATE = new OperationType(
            "WAREHOUSE_UPDATE", "Warehouse Update", "A warehouse was updated or its status changed.");
    public static final OperationType PRODUCT_REGISTRATION = new OperationType(
            "PRODUCT_REGISTRATION", "Product Registration", "A product was registered in the catalog.");
    public static final OperationType PRODUCT_UPDATE = new OperationType(
            "PRODUCT_UPDATE", "Product Update", "A product or its variants were updated.");
    public static final OperationType PRODUCT_PUBLICATION = new OperationType(
            "PRODUCT_PUBLICATION", "Product Publication", "A product was published.");
    public static final OperationType PRODUCT_SUSPENSION = new OperationType(
            "PRODUCT_SUSPENSION", "Product Suspension", "A product was suspended.");
    public static final OperationType PRODUCT_DISCONTINUATION = new OperationType(
            "PRODUCT_DISCONTINUATION", "Product Discontinuation", "A product was discontinued.");
    public static final OperationType INVENTORY_INBOUND = new OperationType(
            "INVENTORY_INBOUND", "Inventory Inbound", "Stock entered a warehouse.");
    public static final OperationType INVENTORY_RESERVATION = new OperationType(
            "INVENTORY_RESERVATION", "Inventory Reservation", "Stock was reserved for an order.");
    public static final OperationType INVENTORY_SALE_OUTBOUND = new OperationType(
            "INVENTORY_SALE_OUTBOUND", "Inventory Sale Outbound", "Reserved stock left the warehouse due to a sale.");
    public static final OperationType INVENTORY_ADJUSTMENT = new OperationType(
            "INVENTORY_ADJUSTMENT", "Inventory Adjustment", "Stock levels or condition were manually adjusted.");
    public static final OperationType INVENTORY_RETURN = new OperationType(
            "INVENTORY_RETURN", "Inventory Return", "Returned stock re-entered a warehouse.");
    public static final OperationType CART_UPDATE = new OperationType(
            "CART_UPDATE", "Cart Update", "Items were added, changed or removed in a cart.");
    public static final OperationType ORDER_CONFIRMATION = new OperationType(
            "ORDER_CONFIRMATION", "Order Confirmation", "A cart was confirmed as an order.");
    public static final OperationType ORDER_PAYMENT = new OperationType(
            "ORDER_PAYMENT", "Order Payment", "Payment of an order was confirmed.");
    public static final OperationType ORDER_DISPATCH = new OperationType(
            "ORDER_DISPATCH", "Order Dispatch", "An order was dispatched from its warehouses.");
    public static final OperationType ORDER_FINALIZATION = new OperationType(
            "ORDER_FINALIZATION", "Order Finalization", "An order was finalized after delivery.");
    public static final OperationType INVOICE_ISSUED = new OperationType(
            "INVOICE_ISSUED", "Invoice Issued", "An invoice was issued.");
    public static final OperationType INVOICE_ANNULLED = new OperationType(
            "INVOICE_ANNULLED", "Invoice Annulled", "An invoice was annulled.");
    public static final OperationType SHIPMENT_UPDATE = new OperationType(
            "SHIPMENT_UPDATE", "Shipment Update", "The logistics status of a shipment changed.");
    public static final OperationType RETURN_REQUEST = new OperationType(
            "RETURN_REQUEST", "Return Request", "A buyer requested a return.");
    public static final OperationType RETURN_APPROVAL = new OperationType(
            "RETURN_APPROVAL", "Return Approval", "A return request was approved.");
    public static final OperationType RETURN_REJECTION = new OperationType(
            "RETURN_REJECTION", "Return Rejection", "A return request was rejected.");
    public static final OperationType RETURN_RECEPTION = new OperationType(
            "RETURN_RECEPTION", "Return Reception", "Returned goods were received.");
    public static final OperationType REFUND_REQUEST = new OperationType(
            "REFUND_REQUEST", "Refund Request", "A refund was requested.");
    public static final OperationType REFUND_APPROVAL = new OperationType(
            "REFUND_APPROVAL", "Refund Approval", "A refund was approved.");
    public static final OperationType REFUND_REJECTION = new OperationType(
            "REFUND_REJECTION", "Refund Rejection", "A refund was rejected.");
    public static final OperationType REFUND_PROCESSING = new OperationType(
            "REFUND_PROCESSING", "Refund Processing", "A refund was paid to the buyer.");
    public static final OperationType USER_REGISTRATION = new OperationType(
            "USER_REGISTRATION", "User Registration", "A staff user was registered by an administrator.");
    public static final OperationType USER_STATUS_CHANGE = new OperationType(
            "USER_STATUS_CHANGE", "User Status Change", "The access status of a user was changed.");
    public static final OperationType BUYER_STATUS_CHANGE = new OperationType(
            "BUYER_STATUS_CHANGE", "Buyer Status Change", "The commercial status of a buyer was changed.");
    public static final OperationType SELLER_STATUS_CHANGE = new OperationType(
            "SELLER_STATUS_CHANGE", "Seller Status Change", "The operational status of a seller was changed.");

    private OperationType(String code, String name, String description) {
        super(code, name, description);
    }
}
