package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Variants are stored in product_variant and managed by ProductVariantMySqlAdapter. */
@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_identification", nullable = false)
    private SellerEntity seller;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 30)
    private String status;
}
