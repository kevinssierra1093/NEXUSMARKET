package application.domain.services.buyer;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultBuyerService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Buyer execute(User requestingUser, Buyer buyer) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.BUYER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Buyer stored = buyerRepositoryPort.findByIdentification(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        if (SystemRole.BUYER.equals(requestingUser.getRole())) {
            validateBuyerOwnershipService.execute(requestingUser, stored);
        }
        return stored;
    }

    public List<Buyer> executeAll(User requestingUser) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        return buyerRepositoryPort.findAll();
    }
}
