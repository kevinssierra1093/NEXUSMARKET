package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateBuyerRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String phoneNumber,
        @Size(max = 250) String address,
        @NotBlank @Size(max = 250) String primaryAddress,
        List<@NotBlank @Size(max = 250) String> additionalAddresses) {
}
