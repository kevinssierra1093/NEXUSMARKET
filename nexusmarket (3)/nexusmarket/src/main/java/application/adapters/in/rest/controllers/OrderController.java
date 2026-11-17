package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ReturnRequestResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.models.Order;
import application.domain.services.invoice.ConsultInvoiceService;
import application.domain.services.order.CalculateOrderTotalService;
import application.domain.services.order.ConfirmOrderPaymentService;
import application.domain.services.order.ConfirmOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.DispatchOrderService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.shipment.ConsultShipmentService;
import application.infrastructure.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static application.adapters.shared.ValueObjectResolver.code;

/**
 * Order lifecycle. Each use case runs in ONE transaction: e.g. if the reservation of the last
 * line fails during confirmation, the reservations of the previous lines are rolled back.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Transactional
public class OrderController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConfirmOrderService confirmOrderService;
    private final ConfirmOrderPaymentService confirmOrderPaymentService;
    private final DispatchOrderService dispatchOrderService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultShipmentService consultShipmentService;
    private final CalculateOrderTotalService calculateOrderTotalService;
    private final ConsultReturnService consultReturnService;

    /** BUYER: CART -> PENDING_PAYMENT (reserves stock of physical items). */
    @PostMapping("/{identifier}/confirm")
    public OrderResponse confirm(@PathVariable String identifier) {
        return toResponse(confirmOrderService.execute(authenticatedUser.resolve(), RestReferences.order(identifier)));
    }

    /** BUYER: PENDING_PAYMENT -> PAID (invoice + shipments; digital-only orders are finalized). */
    @PostMapping("/{identifier}/payment")
    public OrderResponse confirmPayment(@PathVariable String identifier) {
        return toResponse(confirmOrderPaymentService.execute(authenticatedUser.resolve(),
                RestReferences.order(identifier)));
    }

    /** LOGISTICS_OPERATOR: PAID -> SHIPPED. */
    @PostMapping("/{identifier}/dispatch")
    public OrderResponse dispatch(@PathVariable String identifier) {
        return toResponse(dispatchOrderService.execute(authenticatedUser.resolve(), RestReferences.order(identifier)));
    }

    /** Includes, for each line, the status of its return (null when the product was not returned). */
    @GetMapping("/{identifier}")
    public OrderResponse consult(@PathVariable String identifier) {
        var requestingUser = authenticatedUser.resolve();
        Order order = consultOrderService.executeById(requestingUser, RestReferences.order(identifier));
        Map<String, String> returnStatusByVariant = consultReturnService.executeByOrder(requestingUser, order).stream()
                .filter(returnRequest -> returnRequest.getVariant() != null)
                .collect(Collectors.toMap(returnRequest -> returnRequest.getVariant().getIdentifier(),
                        returnRequest -> code(returnRequest.getStatus()),
                        OrderController::preferNonRejected));
        return CommercialRestMapper.toResponse(order, calculateOrderTotalService.execute(order), returnStatusByVariant);
    }

    /** Returns requested for the products of this order (one per returned product). */
    @GetMapping("/{identifier}/returns")
    public List<ReturnRequestResponse> returns(@PathVariable String identifier) {
        return consultReturnService.executeByOrder(authenticatedUser.resolve(), RestReferences.order(identifier))
                .stream().map(CommercialRestMapper::toResponse).toList();
    }

    /** A rejected return may be followed by a new one for the same product: the non-rejected status wins. */
    private static String preferNonRejected(String first, String second) {
        return "REJECTED".equals(first) ? second : first;
    }

    /** Operational follow-up, e.g. /api/orders?status=PAID (orders pending dispatch). */
    @GetMapping
    public List<OrderResponse> consultByStatus(@RequestParam String status) {
        return consultOrderService.executeByStatus(authenticatedUser.resolve(), RestReferences.orderStatusFilter(status))
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/{identifier}/invoice")
    public InvoiceResponse invoice(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(
                consultInvoiceService.execute(authenticatedUser.resolve(), RestReferences.order(identifier)));
    }

    @GetMapping("/{identifier}/shipments")
    public List<ShipmentResponse> shipments(@PathVariable String identifier) {
        return consultShipmentService.executeByOrder(authenticatedUser.resolve(), RestReferences.order(identifier))
                .stream().map(CommercialRestMapper::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        return CommercialRestMapper.toResponse(order, calculateOrderTotalService.execute(order));
    }
}
