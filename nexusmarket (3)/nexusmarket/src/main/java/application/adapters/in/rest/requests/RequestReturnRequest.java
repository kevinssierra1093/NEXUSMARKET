package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Return of ONE product of a finalized order: the variant identifies the order line. */
public record RequestReturnRequest(
        @NotBlank String orderIdentifier,
        @NotBlank String variantIdentifier,
        @NotBlank @Size(max = 1000) String reason) {
}
