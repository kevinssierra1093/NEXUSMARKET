package application.domain.services.buyer;

import application.domain.exceptions.DomainException;
import application.domain.models.Buyer;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.BuyerStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Buyer self-registration. This is the only operation that cannot require an authenticated
 * user (RG-01), because the buyer does not have access to the platform yet.
 */
@Service
@RequiredArgsConstructor
public class RegisterBuyerService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public Buyer execute(Buyer buyer) {
        validateRequiredData(buyer);
        validateUniqueness(buyer);
        buyer.setRole(SystemRole.BUYER);
        buyer.setStatus(BuyerStatus.ACTIVE);
        return buyerRepositoryPort.save(buyer);
    }

    private void validateRequiredData(Buyer buyer) {
        if (buyer.getFullName() == null || buyer.getFullName().isBlank()) {
            throw new DomainException("The buyer full name must not be empty.");
        }
        if (buyer.getPrimaryAddress() == null || buyer.getPrimaryAddress().isBlank()) {
            throw new DomainException("The buyer primary address is required.");
        }
    }

    // Identification and email must be unique across the whole platform.
    private void validateUniqueness(Buyer buyer) {
        if (buyerRepositoryPort.existsByIdentification(buyer) || sellerRepositoryPort.existsByIdentification(buyer)
                || userRepositoryPort.existsByIdentification(buyer)) {
            throw new DomainException("A person with identification " + buyer.getIdentification() + " already exists.");
        }
        if (buyerRepositoryPort.existsByEmail(buyer) || sellerRepositoryPort.existsByEmail(buyer)
                || userRepositoryPort.existsByEmail(buyer)) {
            throw new DomainException("A person with email " + buyer.getEmail() + " already exists.");
        }
    }
}
