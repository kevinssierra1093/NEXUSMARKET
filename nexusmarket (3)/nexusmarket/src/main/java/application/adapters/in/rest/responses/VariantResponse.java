package application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.Map;

public record VariantResponse(
        String identifier,
        String productIdentifier,
        Map<String, String> attributes,
        BigDecimal price) {
}
