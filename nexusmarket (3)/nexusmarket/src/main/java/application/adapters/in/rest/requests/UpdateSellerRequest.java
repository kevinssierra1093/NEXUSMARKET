package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSellerRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String phoneNumber,
        @Size(max = 250) String address,
        @NotBlank @Size(max = 150) String businessName) {
}
