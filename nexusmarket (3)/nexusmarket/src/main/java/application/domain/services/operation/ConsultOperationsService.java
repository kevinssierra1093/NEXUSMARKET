package application.domain.services.operation;

import application.domain.models.Operation;
import application.domain.models.TraceableEntity;
import application.domain.models.User;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultOperationsService {

    private final OperationRepositoryPort operationRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public List<Operation> executeByUser(User requestingUser, User targetUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return operationRepositoryPort.findByUser(targetUser);
    }

    public List<Operation> executeByEntity(User requestingUser, TraceableEntity entity) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return operationRepositoryPort.findByAffectedEntity(entity);
    }

    public List<Operation> executeByType(User requestingUser, Operation operation) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return operationRepositoryPort.findByType(operation);
    }
}
