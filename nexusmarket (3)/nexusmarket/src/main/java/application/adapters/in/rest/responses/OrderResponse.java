package application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Also used for the cart (an order in CART status). */
public record OrderResponse(
        String identifier,
        String buyerIdentification,
        String status,
        List<OrderItemResponse> items,
        BigDecimal total,
        LocalDateTime creationDate,
        LocalDateTime paymentDate,
        LocalDateTime dispatchDate,
        LocalDateTime deliveryDate) {
}
