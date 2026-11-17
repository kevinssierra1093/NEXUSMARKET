package application.adapters.in.rest.mappers;

import application.adapters.shared.TraceableEntityReferences;
import application.domain.exceptions.DomainException;
import application.domain.models.AuditLog;
import application.domain.models.Buyer;
import application.domain.models.InventoryItem;
import application.domain.models.Operation;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.Shipment;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ReturnStatus;

import static application.adapters.shared.ValueObjectResolver.fromCode;

/**
 * Builds "reference models" from the identifiers received in paths and query parameters:
 * Domain Models carrying only their identifier (or the filter value). The domain services load
 * the complete model through their Output Ports.
 */
public final class RestReferences {

    private RestReferences() {
    }

    public static User user(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    public static Buyer buyer(String identification) {
        Buyer buyer = new Buyer();
        buyer.setIdentification(identification);
        return buyer;
    }

    public static Seller seller(String identification) {
        Seller seller = new Seller();
        seller.setIdentification(identification);
        return seller;
    }

    public static Warehouse warehouse(String identifier) {
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentifier(identifier);
        return warehouse;
    }

    public static Product product(String identifier) {
        Product product = new Product();
        product.setIdentifier(identifier);
        return product;
    }

    public static ProductVariant variant(String identifier) {
        ProductVariant variant = new ProductVariant();
        variant.setIdentifier(identifier);
        return variant;
    }

    public static InventoryItem inventoryItem(String identifier) {
        InventoryItem item = new InventoryItem();
        item.setIdentifier(identifier);
        return item;
    }

    public static Order order(String identifier) {
        Order order = new Order();
        order.setIdentifier(identifier);
        return order;
    }

    public static Shipment shipment(String identifier) {
        Shipment shipment = new Shipment();
        shipment.setIdentifier(identifier);
        return shipment;
    }

    public static ReturnRequest returnRequest(String identifier) {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setIdentifier(identifier);
        return returnRequest;
    }

    public static Refund refund(String identifier) {
        Refund refund = new Refund();
        refund.setIdentifier(identifier);
        return refund;
    }

    // ------------------------------------------------------------- filters

    public static Order orderStatusFilter(String statusCode) {
        Order filter = new Order();
        filter.setOrderStatus(fromCode(OrderStatus.class, statusCode));
        return filter;
    }

    public static ReturnRequest returnStatusFilter(String statusCode) {
        ReturnRequest filter = new ReturnRequest();
        filter.setStatus(fromCode(ReturnStatus.class, statusCode));
        return filter;
    }

    public static Operation operationTypeFilter(String typeCode) {
        Operation filter = new Operation();
        filter.setOperationType(fromCode(OperationType.class, typeCode));
        return filter;
    }

    public static AuditLog auditTypeFilter(String typeCode) {
        AuditLog filter = new AuditLog();
        filter.setOperationType(fromCode(OperationType.class, typeCode));
        return filter;
    }

    /** entityType: simple class name of the affected entity (Order, Product, InventoryItem, ...). */
    public static TraceableEntity traceableEntity(String entityType, String identifier) {
        TraceableEntity entity = TraceableEntityReferences.reference(entityType, identifier);
        if (entity == null) {
            throw new DomainException("Unknown entity type: " + entityType + ".");
        }
        return entity;
    }
}
