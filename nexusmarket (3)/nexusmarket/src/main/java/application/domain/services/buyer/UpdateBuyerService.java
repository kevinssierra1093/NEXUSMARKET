package application.domain.services.buyer;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.services.authorization.ValidateBuyerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/** A buyer only administers their own data (contact data, primary and additional addresses). */
@Service
@RequiredArgsConstructor
public class UpdateBuyerService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Buyer execute(User requestingUser, Buyer buyer) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        Buyer stored = buyerRepositoryPort.findByIdentification(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Buyer"));
        validateBuyerOwnershipService.execute(requestingUser, stored);
        if (buyer.getPrimaryAddress() == null || buyer.getPrimaryAddress().isBlank()) {
            throw new DomainException("The buyer primary address is required.");
        }
        stored.setFullName(buyer.getFullName());
        stored.setPhoneNumber(buyer.getPhoneNumber());
        stored.setAddress(buyer.getAddress());
        stored.setPrimaryAddress(buyer.getPrimaryAddress());
        stored.setAdditionalAddresses(new ArrayList<>(buyer.getAdditionalAddresses()));
        buyerRepositoryPort.update(stored);
        return stored;
    }
}
