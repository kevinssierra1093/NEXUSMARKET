package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Creates the system access (User) of an already registered Buyer or Seller. */
@Service
@RequiredArgsConstructor
public class RegisterParticipantUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public User execute(User user) {
        validateParticipantAssociation(user);
        validateUsernameUniqueness(user);
        validateUserIdentityUniqueness(user);
        user.setPassword(passwordServicePort.encrypt(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(user);
    }

    private void validateParticipantAssociation(User user) {
        if (user.getParticipant() == null) {
            throw new DomainException("A buyer or seller association is required for participant user registration.");
        }
        if (SystemRole.BUYER.equals(user.getRole()) && user.getParticipant() instanceof Buyer buyer) {
            buyerRepositoryPort.findByIdentification(buyer)
                    .orElseThrow(() -> new EntityNotFoundException("Associated buyer"));
        } else if (SystemRole.SELLER.equals(user.getRole()) && user.getParticipant() instanceof Seller seller) {
            sellerRepositoryPort.findByIdentification(seller)
                    .orElseThrow(() -> new EntityNotFoundException("Associated seller"));
        } else {
            throw new DomainException("The user role must be BUYER or SELLER and match the associated participant.");
        }
    }

    private void validateUsernameUniqueness(User user) {
        if (userRepositoryPort.existsByUsername(user)) {
            throw new DomainException("Username " + user.getUsername() + " is already in use.");
        }
    }

    private void validateUserIdentityUniqueness(User user) {
        if (userRepositoryPort.existsByIdentification(user)) {
            throw new DomainException("A user with identification " + user.getIdentification() + " already exists.");
        }
        if (userRepositoryPort.existsByEmail(user)) {
            throw new DomainException("A user with email " + user.getEmail() + " already exists.");
        }
    }
}
