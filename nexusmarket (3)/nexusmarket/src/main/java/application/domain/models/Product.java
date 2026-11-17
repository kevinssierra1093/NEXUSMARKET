package application.domain.models;

import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Product extends TraceableEntity {
    private Seller seller;
    private String name;
    private String description;
    private ProductType type;
    private ProductStatus status;
    // Populated on demand; empty by default.
    private List<ProductVariant> variants = new ArrayList<>();
}
