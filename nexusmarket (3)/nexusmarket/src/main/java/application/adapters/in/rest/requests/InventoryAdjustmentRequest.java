package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryAdjustmentRequest(
        @NotNull @PositiveOrZero Integer newQuantityAvailable) {
}
