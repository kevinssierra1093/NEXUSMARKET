package application.adapters.in.rest.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Internal staff: role LOGISTICS_OPERATOR, ADMINISTRATOR or SUPERVISOR. */
public record RegisterStaffUserRequest(
        @NotBlank @Size(min = 4, max = 60) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 30) String identification,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phoneNumber,
        @Size(max = 250) String address,
        @NotBlank String role) {
}
