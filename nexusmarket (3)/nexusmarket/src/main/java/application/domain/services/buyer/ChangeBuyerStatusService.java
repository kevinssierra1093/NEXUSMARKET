package application.domain.services.buyer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.BuyerStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChangeBuyerStatusService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Buyer execute(User requestingUser, Buyer buyer, BuyerStatus newStatus) {
        authorizeAdministrationService.execute(requestingUser);
        Buyer stored = buyerRepositoryPort.findByIdentification(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        if (stored.getStatus().equals(newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        buyerRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("buyerIdentification", stored.getIdentification());
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.BUYER_STATUS_CHANGE, null, details);
        return stored;
    }
}
