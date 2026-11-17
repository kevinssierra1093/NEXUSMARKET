package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Refund;
import application.domain.models.User;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApproveRefundService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Refund execute(User requestingUser, Refund refund) {
        authorizeAdministrationService.execute(requestingUser);
        Refund stored = refundRepositoryPort.findByIdentifier(refund)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
        if (!RefundStatus.REQUESTED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), RefundStatus.APPROVED.getCode());
        }
        stored.setStatus(RefundStatus.APPROVED);
        refundRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", RefundStatus.REQUESTED.getCode());
        details.put("newStatus", RefundStatus.APPROVED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.REFUND_APPROVAL, stored, details);
        return stored;
    }
}
