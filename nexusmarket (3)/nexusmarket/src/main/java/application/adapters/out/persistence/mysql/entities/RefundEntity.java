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
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
public class RefundEntity {

    @Id
    @Column(length = 36)
    private String identifier;

    @OneToOne(optional = false)
    @JoinColumn(name = "return_request_identifier", nullable = false, unique = true)
    private ReturnRequestEntity returnRequest;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 30)
    private String status;

    private LocalDateTime processedDate;
}
