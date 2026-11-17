package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.shared.TraceableEntityReferences;
import application.domain.models.AuditLog;
import application.domain.models.User;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;

import java.util.HashMap;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

public final class AuditLogDocumentMapper {

    private AuditLogDocumentMapper() {
    }

    public static AuditLogDocument toDocument(AuditLog auditLog) {
        AuditLogDocument document = new AuditLogDocument();
        document.setId(auditLog.getAuditId());
        document.setOperationType(code(auditLog.getOperationType()));
        document.setOperationDate(auditLog.getOperationDate());
        document.setUserRole(code(auditLog.getUserRole()));
        User user = auditLog.getPerformedBy();
        if (user != null) {
            AuditLogDocument.UserSnapshot snapshot = new AuditLogDocument.UserSnapshot();
            snapshot.setUserId(user.getUserId());
            snapshot.setUsername(user.getUsername());
            snapshot.setRole(code(user.getRole()));
            document.setPerformedBy(snapshot);
        }
        if (auditLog.getAffectedEntity() != null) {
            AuditLogDocument.EntityReference reference = new AuditLogDocument.EntityReference();
            reference.setType(TraceableEntityReferences.typeOf(auditLog.getAffectedEntity()));
            reference.setIdentifier(TraceableEntityReferences.identifierOf(auditLog.getAffectedEntity()));
            document.setAffectedEntity(reference);
        }
        if (auditLog.getDetails() != null) {
            document.setDetails(new HashMap<>(auditLog.getDetails()));
        }
        return document;
    }

    /** performedBy is rebuilt as a reference User (id, username, role). */
    public static AuditLog toDomain(AuditLogDocument document) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(document.getId());
        auditLog.setOperationType(fromCode(OperationType.class, document.getOperationType()));
        auditLog.setOperationDate(document.getOperationDate());
        auditLog.setUserRole(fromCode(SystemRole.class, document.getUserRole()));
        if (document.getPerformedBy() != null) {
            User user = new User();
            user.setUserId(document.getPerformedBy().getUserId());
            user.setUsername(document.getPerformedBy().getUsername());
            user.setRole(fromCode(SystemRole.class, document.getPerformedBy().getRole()));
            auditLog.setPerformedBy(user);
        }
        if (document.getAffectedEntity() != null) {
            auditLog.setAffectedEntity(TraceableEntityReferences.reference(
                    document.getAffectedEntity().getType(), document.getAffectedEntity().getIdentifier()));
        }
        auditLog.setDetails(document.getDetails() == null ? new HashMap<>() : new HashMap<>(document.getDetails()));
        return auditLog;
    }
}
