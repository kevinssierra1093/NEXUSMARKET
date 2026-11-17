package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.Map;

/** Return of one product: identifies the product, its seller and the returned quantity. */
public record ReturnRequestResponse(
        String identifier,
        String orderIdentifier,
        String buyerIdentification,
        String variantIdentifier,
        Map<String, String> variantAttributes,
        String productIdentifier,
        String productName,
        String productType,
        String sellerIdentification,
        String sellerBusinessName,
        Integer quantity,
        String reason,
        String status,
        LocalDateTime requestDate,
        LocalDateTime resolutionDate) {
}
