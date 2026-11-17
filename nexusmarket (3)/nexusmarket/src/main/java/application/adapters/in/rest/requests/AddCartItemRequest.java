package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
        @NotBlank String variantIdentifier,
        @NotNull @Positive Integer quantity) {
}
