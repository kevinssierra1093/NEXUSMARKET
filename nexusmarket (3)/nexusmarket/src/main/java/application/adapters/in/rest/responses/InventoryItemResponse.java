package application.adapters.in.rest.responses;

import java.util.Map;

public record InventoryItemResponse(
        String identifier,
        String variantIdentifier,
        Map<String, String> variantAttributes,
        String productIdentifier,
        String productName,
        String warehouseIdentifier,
        String warehouseName,
        Integer quantityAvailable,
        Integer quantityReserved,
        String status) {
}
