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
@Table(name = "return_request")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequestEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_identifier", nullable = false)
    private OrderEntity order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_identification", nullable = false)
    private BuyerEntity buyer;

    /** Returned product (one line of the order). */
    @ManyToOne
    @JoinColumn(name = "variant_identifier")
    private ProductVariantEntity variant;

    private Integer quantity;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    private LocalDateTime resolutionDate;
}
