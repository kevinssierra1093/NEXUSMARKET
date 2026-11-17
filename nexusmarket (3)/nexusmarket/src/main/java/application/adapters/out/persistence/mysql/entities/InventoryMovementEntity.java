package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Immutable stock movement event. */
@Entity
@Table(name = "inventory_movement")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movementId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventory_item_identifier", nullable = false)
    private InventoryItemEntity inventoryItem;

    @Column(nullable = false, length = 30)
    private String movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime movementDate;

    @ManyToOne
    @JoinColumn(name = "performed_by")
    private UserEntity performedBy;

    @ManyToOne
    @JoinColumn(name = "related_order_identifier")
    private OrderEntity relatedOrder;
}
