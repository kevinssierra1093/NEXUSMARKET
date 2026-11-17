package application.domain.services.returns;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ReturnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RejectReturnService {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public ReturnRequest execute(User requestingUser, ReturnRequest request) {
        authorizeAdministrationService.execute(requestingUser);
        ReturnRequest stored = returnRequestRepositoryPort.findByIdentifier(request)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        if (!ReturnStatus.REQUESTED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), ReturnStatus.REJECTED.getCode());
        }
        stored.setStatus(ReturnStatus.REJECTED);
        stored.setResolutionDate(LocalDateTime.now());
        returnRequestRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", ReturnStatus.REQUESTED.getCode());
        details.put("newStatus", ReturnStatus.REJECTED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.RETURN_REJECTION, stored, details);
        return stored;
    }
}
