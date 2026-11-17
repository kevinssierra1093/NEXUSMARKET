package application.domain.models;

import application.domain.valueobjects.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Invoice extends TraceableEntity {
    private Order order;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
}
