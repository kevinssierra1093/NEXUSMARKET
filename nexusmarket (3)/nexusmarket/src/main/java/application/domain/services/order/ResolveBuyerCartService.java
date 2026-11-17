package application.domain.services.order;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.BuyerStatus;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * The cart is the Order of a buyer while its status is CART. This service resolves the
 * authenticated buyer and returns (or creates) that order.
 */
@Service
@RequiredArgsConstructor
public class ResolveBuyerCartService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public Order findOrCreate(User requestingUser) {
        Buyer buyer = resolveEnabledBuyer(requestingUser);
        return orderRepositoryPort.findCartByBuyer(buyer).orElseGet(() -> {
            Order cart = new Order();
            cart.setBuyer(buyer);
            cart.setOrderStatus(OrderStatus.CART);
            cart.setCreationDate(LocalDateTime.now());
            return orderRepositoryPort.save(cart);
        });
    }

    public Order find(User requestingUser) {
        Buyer buyer = resolveEnabledBuyer(requestingUser);
        return orderRepositoryPort.findCartByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Cart"));
    }

    private Buyer resolveEnabledBuyer(User user) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.BUYER);
        if (!(user.getParticipant() instanceof Buyer participant)) {
            throw new UnauthorizedOperationException("The user is not associated with a buyer.");
        }
        Buyer buyer = buyerRepositoryPort.findByIdentification(participant)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        if (!BuyerStatus.ACTIVE.equals(buyer.getStatus())) {
            throw new DomainException("The buyer is not enabled to make purchases (status "
                    + buyer.getStatus().getCode() + ").");
        }
        return buyer;
    }
}
