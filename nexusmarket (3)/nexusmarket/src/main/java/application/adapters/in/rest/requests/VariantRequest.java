package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;

/** attributes: e.g. {"color": "red", "size": "M"}; may be empty for products without variants. */
public record VariantRequest(
        Map<String, String> attributes,
        @NotNull @Positive BigDecimal price) {
}
