package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** type: MARKETPLACE or SELLER. ownerIdentification is required only for SELLER warehouses. */
public record RegisterWarehouseRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 250) String address,
        @NotBlank String type,
        String ownerIdentification) {
}
