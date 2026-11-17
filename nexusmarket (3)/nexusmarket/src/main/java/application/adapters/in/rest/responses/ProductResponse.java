package application.adapters.in.rest.responses;

import java.util.List;

/** variants is empty in listings; it is populated when consulting a single product. */
public record ProductResponse(
        String identifier,
        String sellerIdentification,
        String sellerBusinessName,
        String name,
        String description,
        String type,
        String status,
        List<VariantResponse> variants) {
}
