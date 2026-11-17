package application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        String identifier,
        String returnRequestIdentifier,
        String orderIdentifier,
        BigDecimal amount,
        String status,
        LocalDateTime processedDate) {
}
