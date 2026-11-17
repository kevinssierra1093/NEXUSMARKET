package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InvoiceEntity;
import application.adapters.out.persistence.mysql.entities.OrderEntity;
import application.adapters.out.persistence.mysql.entities.OrderItemEntity;
import application.adapters.out.persistence.mysql.entities.RefundEntity;
import application.adapters.out.persistence.mysql.entities.ReturnRequestEntity;
import application.adapters.out.persistence.mysql.entities.ShipmentEntity;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;
import application.domain.valueobjects.InvoiceStatus;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.RefundStatus;
import application.domain.valueobjects.ReturnStatus;
import application.domain.valueobjects.ShipmentStatus;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Order, OrderItem, Invoice, Shipment, ReturnRequest and Refund <-> JPA entities. */
public final class CommercialPersistenceMapper {

    private CommercialPersistenceMapper() {
    }

    // ----------------------------------------------------------------- Order

    public static Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }
        Order order = new Order();
        order.setIdentifier(entity.getIdentifier());
        order.setBuyer(ParticipantPersistenceMapper.toDomain(entity.getBuyer()));
        order.setOrderStatus(fromCode(OrderStatus.class, entity.getOrderStatus()));
        order.setCreationDate(entity.getCreationDate());
        order.setPaymentDate(entity.getPaymentDate());
        order.setDispatchDate(entity.getDispatchDate());
        order.setDeliveryDate(entity.getDeliveryDate());
        for (OrderItemEntity itemEntity : entity.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setVariant(CatalogPersistenceMapper.toDomain(itemEntity.getVariant()));
            item.setQuantity(itemEntity.getQuantity());
            item.setUnitPrice(itemEntity.getUnitPrice());
            item.setWarehouse(CatalogPersistenceMapper.toDomain(itemEntity.getWarehouse()));
            order.getItems().add(item);
        }
        return order;
    }

    /** Scalar attributes; buyer and lines are synchronized by OrderMySqlAdapter. */
    public static void copy(Order order, OrderEntity entity) {
        entity.setOrderStatus(code(order.getOrderStatus()));
        entity.setCreationDate(order.getCreationDate());
        entity.setPaymentDate(order.getPaymentDate());
        entity.setDispatchDate(order.getDispatchDate());
        entity.setDeliveryDate(order.getDeliveryDate());
    }

    // --------------------------------------------------------------- Invoice

    public static Invoice toDomain(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }
        Invoice invoice = new Invoice();
        invoice.setIdentifier(entity.getIdentifier());
        invoice.setOrder(toDomain(entity.getOrder()));
        invoice.setIssueDate(entity.getIssueDate());
        invoice.setTotalAmount(entity.getTotalAmount());
        invoice.setStatus(fromCode(InvoiceStatus.class, entity.getStatus()));
        return invoice;
    }

    public static void copy(Invoice invoice, InvoiceEntity entity) {
        entity.setIssueDate(invoice.getIssueDate());
        entity.setTotalAmount(invoice.getTotalAmount());
        entity.setStatus(code(invoice.getStatus()));
    }

    // -------------------------------------------------------------- Shipment

    public static Shipment toDomain(ShipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        Shipment shipment = new Shipment();
        shipment.setIdentifier(entity.getIdentifier());
        shipment.setOrder(toDomain(entity.getOrder()));
        shipment.setOriginWarehouse(CatalogPersistenceMapper.toDomain(entity.getOriginWarehouse()));
        shipment.setHandledBy(ParticipantPersistenceMapper.toDomain(entity.getHandledBy()));
        shipment.setStatus(fromCode(ShipmentStatus.class, entity.getStatus()));
        shipment.setDispatchDate(entity.getDispatchDate());
        shipment.setDeliveryDate(entity.getDeliveryDate());
        return shipment;
    }

    public static void copy(Shipment shipment, ShipmentEntity entity) {
        entity.setStatus(code(shipment.getStatus()));
        entity.setDispatchDate(shipment.getDispatchDate());
        entity.setDeliveryDate(shipment.getDeliveryDate());
    }

    // --------------------------------------------------------- ReturnRequest

    public static ReturnRequest toDomain(ReturnRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        ReturnRequest request = new ReturnRequest();
        request.setIdentifier(entity.getIdentifier());
        request.setOrder(toDomain(entity.getOrder()));
        request.setBuyer(ParticipantPersistenceMapper.toDomain(entity.getBuyer()));
        request.setVariant(CatalogPersistenceMapper.toDomain(entity.getVariant()));
        request.setQuantity(entity.getQuantity());
        request.setReason(entity.getReason());
        request.setStatus(fromCode(ReturnStatus.class, entity.getStatus()));
        request.setRequestDate(entity.getRequestDate());
        request.setResolutionDate(entity.getResolutionDate());
        return request;
    }

    public static void copy(ReturnRequest request, ReturnRequestEntity entity) {
        entity.setQuantity(request.getQuantity());
        entity.setReason(request.getReason());
        entity.setStatus(code(request.getStatus()));
        entity.setRequestDate(request.getRequestDate());
        entity.setResolutionDate(request.getResolutionDate());
    }

    // ---------------------------------------------------------------- Refund

    public static Refund toDomain(RefundEntity entity) {
        if (entity == null) {
            return null;
        }
        Refund refund = new Refund();
        refund.setIdentifier(entity.getIdentifier());
        refund.setReturnRequest(toDomain(entity.getReturnRequest()));
        refund.setAmount(entity.getAmount());
        refund.setStatus(fromCode(RefundStatus.class, entity.getStatus()));
        refund.setProcessedDate(entity.getProcessedDate());
        return refund;
    }

    public static void copy(Refund refund, RefundEntity entity) {
        entity.setAmount(refund.getAmount());
        entity.setStatus(code(refund.getStatus()));
        entity.setProcessedDate(refund.getProcessedDate());
    }
}
