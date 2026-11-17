package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

/** Generic status change: the code of the target status (e.g. "BLOCKED", "IN_TRANSIT"). */
public record ChangeStatusRequest(
        @NotBlank String status) {
}
