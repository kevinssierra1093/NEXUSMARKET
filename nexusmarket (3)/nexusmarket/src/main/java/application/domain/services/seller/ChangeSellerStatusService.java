package application.domain.services.seller;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SellerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChangeSellerStatusService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Seller execute(User requestingUser, Seller seller, SellerStatus newStatus) {
        authorizeAdministrationService.execute(requestingUser);
        Seller stored = sellerRepositoryPort.findByIdentification(seller)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        if (stored.getStatus().equals(newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        sellerRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("sellerIdentification", stored.getIdentification());
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.SELLER_STATUS_CHANGE, null, details);
        return stored;
    }
}
