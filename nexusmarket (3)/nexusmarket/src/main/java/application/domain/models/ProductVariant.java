package application.domain.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductVariant extends TraceableEntity {
    private Product product;
    private Map<String, String> attributes = new HashMap<>();
    private BigDecimal price;
}
