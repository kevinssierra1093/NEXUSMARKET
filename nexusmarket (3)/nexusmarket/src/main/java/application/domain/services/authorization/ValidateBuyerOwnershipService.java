package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/** A buyer never administers information belonging to another buyer. */
@Service
@RequiredArgsConstructor
public class ValidateBuyerOwnershipService {

    public void execute(User user, Buyer buyer) {
        boolean owner = SystemRole.BUYER.equals(user.getRole())
                && user.getParticipant() instanceof Buyer
                && Objects.equals(user.getParticipant().getIdentification(), buyer.getIdentification());
        if (!owner) {
            throw new UnauthorizedOperationException(
                    "The buyer does not have access to information of other buyers.");
        }
    }
}
