package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryInboundRequest(
        @NotBlank String variantIdentifier,
        @NotBlank String warehouseIdentifier,
        @NotNull @Positive Integer quantity) {
}
