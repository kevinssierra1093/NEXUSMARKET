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

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_identifier", nullable = false)
    private OrderEntity order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_warehouse_identifier", nullable = false)
    private WarehouseEntity originWarehouse;

    /** Logistics operator; assigned when the shipment is dispatched. */
    @ManyToOne
    @JoinColumn(name = "handled_by")
    private UserEntity handledBy;

    @Column(nullable = false, length = 30)
    private String status;

    private LocalDateTime dispatchDate;

    private LocalDateTime deliveryDate;
}
