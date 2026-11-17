package application.domain.services.order;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeOrderConsultationService;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultOrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeOrderConsultationService authorizeOrderConsultationService;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Order executeById(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        authorizeOrderConsultationService.execute(requestingUser, stored);
        return stored;
    }

    public List<Order> executeByBuyer(User requestingUser, Buyer buyer) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER, SystemRole.SUPERVISOR);
        if (SystemRole.BUYER.equals(requestingUser.getRole())) {
            validateBuyerOwnershipService.execute(requestingUser, buyer);
        }
        return orderRepositoryPort.findByBuyer(buyer);
    }

    /** Operational follow-up (e.g. PAID orders pending dispatch). */
    public List<Order> executeByStatus(User requestingUser, Order filter) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.LOGISTICS_OPERATOR, SystemRole.SUPERVISOR, SystemRole.ADMINISTRATOR);
        return orderRepositoryPort.findByStatus(filter);
    }
}
