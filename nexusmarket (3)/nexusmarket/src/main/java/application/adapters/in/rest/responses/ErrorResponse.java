package application.adapters.in.rest.responses;

import java.time.LocalDateTime;
import java.util.Map;

/** Uniform error body. fieldErrors is only present for request validation errors. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {
}
