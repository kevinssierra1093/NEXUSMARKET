package application.adapters.out.persistence.mongodb.documents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable audit trail entry. The user and the affected entity are stored as snapshots
 * (identifier + a few descriptive fields), not as full objects.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogDocument {

    @Id
    private String id;

    @Indexed
    private String operationType;

    private LocalDateTime operationDate;

    private UserSnapshot performedBy;

    private String userRole;

    private EntityReference affectedEntity;

    private Map<String, Object> details = new HashMap<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserSnapshot {
        private Integer userId;
        private String username;
        private String role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EntityReference {
        private String type;
        private String identifier;
    }
}
