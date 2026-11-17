package application.domain.services.returns;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeOrderConsultationService;
import application.domain.services.authorization.AuthorizeRefundOperationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultReturnService {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeRefundOperationService authorizeRefundOperationService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;
    private final AuthorizeOrderConsultationService authorizeOrderConsultationService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public ReturnRequest executeById(User requestingUser, ReturnRequest request) {
        ReturnRequest stored = returnRequestRepositoryPort.findByIdentifier(request)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        authorizeRefundOperationService.executeConsultation(requestingUser, stored.getBuyer());
        return stored;
    }

    public List<ReturnRequest> executeByBuyer(User requestingUser, Buyer buyer) {
        authorizeRefundOperationService.executeConsultation(requestingUser, buyer);
        return returnRequestRepositoryPort.findByBuyer(buyer);
    }

    public List<ReturnRequest> executeByStatus(User requestingUser, ReturnRequest filter) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return returnRequestRepositoryPort.findByStatus(filter);
    }

    /**
     * Returns of one order, to see the state of each returned line. Allowed for whoever can
     * consult the order (owning buyer, involved seller, logistics operator, supervisor) and for
     * administrators, who resolve the returns.
     */
    public List<ReturnRequest> executeByOrder(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        if (requestingUser != null && SystemRole.ADMINISTRATOR.equals(requestingUser.getRole())) {
            validateUserAuthorizationStatusService.execute(requestingUser);
        } else {
            authorizeOrderConsultationService.execute(requestingUser, stored);
        }
        return returnRequestRepositoryPort.findByOrder(stored);
    }
}
