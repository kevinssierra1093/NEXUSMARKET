package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The administrator incorporates the seller together with its first warehouse. */
public record RegisterSellerRequest(
        @NotBlank @Size(max = 30) String identification,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phoneNumber,
        @Size(max = 250) String address,
        @NotBlank @Size(max = 150) String businessName,
        @NotNull @Valid WarehouseDataRequest firstWarehouse) {
}
