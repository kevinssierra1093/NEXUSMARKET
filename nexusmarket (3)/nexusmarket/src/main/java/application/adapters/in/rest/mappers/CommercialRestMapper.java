package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.RequestReturnRequest;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderItemResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnRequestResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Shipment;

import java.math.BigDecimal;
import java.util.Map;

import static application.adapters.shared.ValueObjectResolver.code;

/** Orders, invoices, shipments, returns and refunds. */
public final class CommercialRestMapper {

    private CommercialRestMapper() {
    }

    // ------------------------------------------------------------- requests

    public static ReturnRequest toReturnRequest(RequestReturnRequest request) {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(RestReferences.order(request.orderIdentifier()));
        returnRequest.setVariant(RestReferences.variant(request.variantIdentifier()));
        returnRequest.setReason(request.reason());
        return returnRequest;
    }

    // ------------------------------------------------------------ responses

    /** The total is calculated by the domain (CalculateOrderTotalService) and passed in. */
    public static OrderResponse toResponse(Order order, BigDecimal total) {
        return toResponse(order, total, Map.of());
    }

    /** returnStatusByVariant: status of the return of each returned line, keyed by variant identifier. */
    public static OrderResponse toResponse(Order order, BigDecimal total, Map<String, String> returnStatusByVariant) {
        return new OrderResponse(
                order.getIdentifier(),
                order.getBuyer() == null ? null : order.getBuyer().getIdentification(),
                code(order.getOrderStatus()),
                order.getItems().stream().map(item -> toResponse(item, returnStatusByVariant)).toList(),
                total,
                order.getCreationDate(),
                order.getPaymentDate(),
                order.getDispatchDate(),
                order.getDeliveryDate());
    }

    public static OrderItemResponse toResponse(OrderItem item, Map<String, String> returnStatusByVariant) {
        Product product = item.getVariant() == null ? null : item.getVariant().getProduct();
        BigDecimal subtotal = item.getUnitPrice() == null || item.getQuantity() == null ? null
                : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        String variantIdentifier = item.getVariant() == null ? null : item.getVariant().getIdentifier();
        return new OrderItemResponse(
                variantIdentifier,
                CatalogRestMapper.attributes(item.getVariant()),
                product == null ? null : product.getIdentifier(),
                product == null ? null : product.getName(),
                product == null ? null : code(product.getType()),
                product == null || product.getSeller() == null ? null : product.getSeller().getIdentification(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal,
                item.getWarehouse() == null ? null : item.getWarehouse().getIdentifier(),
                variantIdentifier == null ? null : returnStatusByVariant.get(variantIdentifier));
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getIdentifier(),
                invoice.getOrder() == null ? null : invoice.getOrder().getIdentifier(),
                invoice.getIssueDate(),
                invoice.getTotalAmount(),
                code(invoice.getStatus()));
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getIdentifier(),
                shipment.getOrder() == null ? null : shipment.getOrder().getIdentifier(),
                shipment.getOriginWarehouse() == null ? null : shipment.getOriginWarehouse().getIdentifier(),
                shipment.getOriginWarehouse() == null ? null : shipment.getOriginWarehouse().getName(),
                shipment.getHandledBy() == null ? null : shipment.getHandledBy().getUsername(),
                code(shipment.getStatus()),
                shipment.getDispatchDate(),
                shipment.getDeliveryDate());
    }

    public static ReturnRequestResponse toResponse(ReturnRequest returnRequest) {
        ProductVariant variant = returnRequest.getVariant();
        Product product = variant == null ? null : variant.getProduct();
        return new ReturnRequestResponse(
                returnRequest.getIdentifier(),
                returnRequest.getOrder() == null ? null : returnRequest.getOrder().getIdentifier(),
                returnRequest.getBuyer() == null ? null : returnRequest.getBuyer().getIdentification(),
                variant == null ? null : variant.getIdentifier(),
                CatalogRestMapper.attributes(variant),
                product == null ? null : product.getIdentifier(),
                product == null ? null : product.getName(),
                product == null ? null : code(product.getType()),
                product == null || product.getSeller() == null ? null : product.getSeller().getIdentification(),
                product == null || product.getSeller() == null ? null : product.getSeller().getBusinessName(),
                returnRequest.getQuantity(),
                returnRequest.getReason(),
                code(returnRequest.getStatus()),
                returnRequest.getRequestDate(),
                returnRequest.getResolutionDate());
    }

    public static RefundResponse toResponse(Refund refund) {
        ReturnRequest returnRequest = refund.getReturnRequest();
        return new RefundResponse(
                refund.getIdentifier(),
                returnRequest == null ? null : returnRequest.getIdentifier(),
                returnRequest == null || returnRequest.getOrder() == null ? null
                        : returnRequest.getOrder().getIdentifier(),
                refund.getAmount(),
                code(refund.getStatus()),
                refund.getProcessedDate());
    }
}
