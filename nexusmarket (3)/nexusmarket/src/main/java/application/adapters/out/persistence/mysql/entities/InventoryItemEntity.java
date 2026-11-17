package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Distributed stock: one record per (variant, warehouse). */
@Entity
@Table(name = "inventory_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_variant_warehouse",
                columnNames = {"variant_identifier", "warehouse_identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class InventoryItemEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "variant_identifier", nullable = false)
    private ProductVariantEntity variant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_identifier", nullable = false)
    private WarehouseEntity warehouse;

    @Column(nullable = false)
    private Integer quantityAvailable;

    @Column(nullable = false)
    private Integer quantityReserved;

    @Column(nullable = false, length = 30)
    private String status;
}
