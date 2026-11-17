package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** "order" is a SQL reserved word, hence the table name "orders". */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_identification", nullable = false)
    private BuyerEntity buyer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(nullable = false, length = 30)
    private String orderStatus;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    private LocalDateTime paymentDate;

    private LocalDateTime dispatchDate;

    private LocalDateTime deliveryDate;
}
