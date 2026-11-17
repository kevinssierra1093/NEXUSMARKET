package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/** A seller only administers its own products and warehouses. */
@Service
@RequiredArgsConstructor
public class ValidateSellerOwnershipService {

    public void execute(User user, Seller seller) {
        boolean owner = SystemRole.SELLER.equals(user.getRole())
                && user.getParticipant() instanceof Seller
                && Objects.equals(user.getParticipant().getIdentification(), seller.getIdentification());
        if (!owner) {
            throw new UnauthorizedOperationException(
                    "The seller does not have access to information of other sellers.");
        }
    }
}
