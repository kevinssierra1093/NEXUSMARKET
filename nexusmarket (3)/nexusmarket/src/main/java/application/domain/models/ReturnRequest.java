package application.domain.models;

import application.domain.valueobjects.ReturnStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Return of ONE product (order line) of a finalized order. The finalized order is never
 * modified: the return refers to it and to the returned variant. The other lines of the order
 * keep their state.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest extends TraceableEntity {

    private Order order;
    private Buyer buyer;
    // Returned product (identifies the line of the order and, through the product, its seller)
    private ProductVariant variant;
    // Units returned: the whole quantity of the order line
    private Integer quantity;
    private String reason;
    private ReturnStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime resolutionDate;
}
