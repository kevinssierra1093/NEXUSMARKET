package application.domain.models;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    private Order order;
    private ProductVariant variant;
    private Integer quantity;
    private BigDecimal unitPrice;
    // Warehouse from which the stock was reserved; null until the order is confirmed
    // (and always null for digital products).
    private Warehouse warehouse;
}
