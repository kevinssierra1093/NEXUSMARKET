package application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.Map;

/**
 * warehouseIdentifier: warehouse the stock was reserved from (null in carts and for digital products).
 * returnStatus: status of the return of this line (null when the product was not returned).
 */
public record OrderItemResponse(
        String variantIdentifier,
        Map<String, String> variantAttributes,
        String productIdentifier,
        String productName,
        String productType,
        String sellerIdentification,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String warehouseIdentifier,
        String returnStatus) {
}
