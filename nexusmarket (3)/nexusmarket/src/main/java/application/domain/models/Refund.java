package application.domain.models;

import application.domain.valueobjects.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Refund extends TraceableEntity {
    private ReturnRequest returnRequest;
    private BigDecimal amount;
    private RefundStatus status;
    private LocalDateTime processedDate;
}
