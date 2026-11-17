package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description) {
}
