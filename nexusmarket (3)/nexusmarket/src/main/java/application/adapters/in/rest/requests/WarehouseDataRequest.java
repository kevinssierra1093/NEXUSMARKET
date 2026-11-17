package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Name and address of a warehouse (first warehouse of a seller, or warehouse update). */
public record WarehouseDataRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 250) String address) {
}
