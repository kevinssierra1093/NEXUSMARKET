package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** The seller is the authenticated user. type: PHYSICAL or DIGITAL. */
public record RegisterProductRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank String type,
        @NotEmpty @Valid List<VariantRequest> variants) {
}
