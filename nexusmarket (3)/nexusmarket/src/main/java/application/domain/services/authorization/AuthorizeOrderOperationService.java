package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Order management (responsibility matrix): the buyer who owns the order, a seller
 * whose products are in the order, or a logistics operator.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeOrderOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public void execute(User user, Order order) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user,
                SystemRole.BUYER, SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR);
        if (SystemRole.BUYER.equals(user.getRole())) {
            validateBuyerOwnershipService.execute(user, order.getBuyer());
        } else if (SystemRole.SELLER.equals(user.getRole())) {
            validateSellerInvolvement(user, order);
        }
    }

    private void validateSellerInvolvement(User user, Order order) {
        boolean involved = order.getItems().stream().anyMatch(item ->
                user.getParticipant() != null
                        && Objects.equals(user.getParticipant().getIdentification(),
                        item.getVariant().getProduct().getSeller().getIdentification()));
        if (!involved) {
            throw new UnauthorizedOperationException(
                    "The seller does not have products in this order.");
        }
    }
}
