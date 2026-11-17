package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Immutable business operation. The affected entity is polymorphic (TraceableEntity), so it is
 * stored as (type, identifier) instead of a foreign key.
 */
@Entity
@Table(name = "operation",
        indexes = @Index(name = "idx_operation_entity",
                columnList = "affected_entity_type, affected_entity_identifier"))
@Getter
@Setter
@NoArgsConstructor
public class OperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer operationId;

    @Column(nullable = false, length = 40)
    private String operationType;

    @Column(nullable = false)
    private LocalDateTime executionDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "performed_by", nullable = false)
    private UserEntity performedBy;

    @Column(name = "affected_entity_type", length = 40)
    private String affectedEntityType;

    @Column(name = "affected_entity_identifier", length = 36)
    private String affectedEntityIdentifier;
}
