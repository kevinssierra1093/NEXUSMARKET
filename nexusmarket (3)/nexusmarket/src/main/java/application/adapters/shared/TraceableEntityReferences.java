package application.adapters.shared;

import application.domain.models.InventoryItem;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.models.TraceableEntity;
import application.domain.models.Warehouse;

/**
 * Operations and audit logs reference the affected entity polymorphically. They are stored as
 * (type, identifier) and rebuilt as a reference model that only carries the identifier.
 */
public final class TraceableEntityReferences {

    private TraceableEntityReferences() {
    }

    public static String typeOf(TraceableEntity entity) {
        return entity == null ? null : entity.getClass().getSimpleName();
    }

    public static String identifierOf(TraceableEntity entity) {
        return entity == null ? null : entity.getIdentifier();
    }

    public static TraceableEntity reference(String type, String identifier) {
        if (type == null || identifier == null) {
            return null;
        }
        TraceableEntity entity = switch (type) {
            case "Warehouse" -> new Warehouse();
            case "Product" -> new Product();
            case "ProductVariant" -> new ProductVariant();
            case "InventoryItem" -> new InventoryItem();
            case "Order" -> new Order();
            case "Invoice" -> new Invoice();
            case "Shipment" -> new Shipment();
            case "ReturnRequest" -> new ReturnRequest();
            case "Refund" -> new Refund();
            default -> null;
        };
        if (entity != null) {
            entity.setIdentifier(identifier);
        }
        return entity;
    }
}
