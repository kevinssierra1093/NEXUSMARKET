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

@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 250)
    private String address;

    @Column(nullable = false, length = 30)
    private String type;

    /** Null for MARKETPLACE warehouses. */
    @ManyToOne
    @JoinColumn(name = "owner_identification")
    private SellerEntity owner;

    @Column(nullable = false, length = 30)
    private String status;
}
