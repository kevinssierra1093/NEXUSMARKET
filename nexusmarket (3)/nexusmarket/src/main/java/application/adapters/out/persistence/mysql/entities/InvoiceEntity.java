package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_identifier", nullable = false, unique = true)
    private OrderEntity order;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status;
}
