package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_identifier", nullable = false)
    private ProductEntity product;

    @ElementCollection
    @CollectionTable(name = "product_variant_attribute",
            joinColumns = @JoinColumn(name = "variant_identifier"))
    @MapKeyColumn(name = "attr_key", length = 60)
    @Column(name = "attr_value", length = 150)
    private Map<String, String> attributes = new HashMap<>();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
}
