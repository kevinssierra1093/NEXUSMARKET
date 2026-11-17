package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByPerformedBy_UserIdOrderByOperationDateDesc(Integer userId);

    List<AuditLogDocument> findByAffectedEntity_TypeAndAffectedEntity_IdentifierOrderByOperationDateAsc(
            String type, String identifier);

    List<AuditLogDocument> findByOperationTypeOrderByOperationDateDesc(String operationType);
}
