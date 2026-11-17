package application.domain.services.seller;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateSellerService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public Seller execute(User requestingUser, Seller seller) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        Seller stored = sellerRepositoryPort.findByIdentification(seller)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        validateSellerOwnershipService.execute(requestingUser, stored);
        if (seller.getBusinessName() == null || seller.getBusinessName().isBlank()) {
            throw new DomainException("The seller business name is required.");
        }
        stored.setFullName(seller.getFullName());
        stored.setPhoneNumber(seller.getPhoneNumber());
        stored.setAddress(seller.getAddress());
        stored.setBusinessName(seller.getBusinessName());
        sellerRepositoryPort.update(stored);
        return stored;
    }
}
