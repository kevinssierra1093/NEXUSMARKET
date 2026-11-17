package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.BuyerEntity;
import application.adapters.out.persistence.mysql.entities.SellerEntity;
import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.domain.models.Buyer;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.BuyerStatus;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

import java.util.ArrayList;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Buyer, Seller and User <-> JPA entities (scalar attributes only; relations are set by the adapters). */
public final class ParticipantPersistenceMapper {

    private ParticipantPersistenceMapper() {
    }

    // ----------------------------------------------------------------- Buyer

    public static Buyer toDomain(BuyerEntity entity) {
        if (entity == null) {
            return null;
        }
        Buyer buyer = new Buyer();
        buyer.setIdentification(entity.getIdentification());
        buyer.setFullName(entity.getFullName());
        buyer.setEmail(entity.getEmail());
        buyer.setPhoneNumber(entity.getPhoneNumber());
        buyer.setAddress(entity.getAddress());
        buyer.setRole(fromCode(SystemRole.class, entity.getRole()));
        buyer.setPrimaryAddress(entity.getPrimaryAddress());
        buyer.setAdditionalAddresses(new ArrayList<>(entity.getAdditionalAddresses()));
        buyer.setStatus(fromCode(BuyerStatus.class, entity.getStatus()));
        return buyer;
    }

    public static void copy(Buyer buyer, BuyerEntity entity) {
        entity.setIdentification(buyer.getIdentification());
        entity.setFullName(buyer.getFullName());
        entity.setEmail(buyer.getEmail());
        entity.setPhoneNumber(buyer.getPhoneNumber());
        entity.setAddress(buyer.getAddress());
        entity.setRole(code(buyer.getRole()));
        entity.setPrimaryAddress(buyer.getPrimaryAddress());
        entity.getAdditionalAddresses().clear();
        if (buyer.getAdditionalAddresses() != null) {
            entity.getAdditionalAddresses().addAll(buyer.getAdditionalAddresses());
        }
        entity.setStatus(code(buyer.getStatus()));
    }

    // ---------------------------------------------------------------- Seller

    /** Warehouses and products are not loaded here (loaded on demand by ConsultSellerService). */
    public static Seller toDomain(SellerEntity entity) {
        if (entity == null) {
            return null;
        }
        Seller seller = new Seller();
        seller.setIdentification(entity.getIdentification());
        seller.setFullName(entity.getFullName());
        seller.setEmail(entity.getEmail());
        seller.setPhoneNumber(entity.getPhoneNumber());
        seller.setAddress(entity.getAddress());
        seller.setRole(fromCode(SystemRole.class, entity.getRole()));
        seller.setBusinessName(entity.getBusinessName());
        seller.setRegistrationDate(entity.getRegistrationDate());
        seller.setStatus(fromCode(SellerStatus.class, entity.getStatus()));
        return seller;
    }

    public static void copy(Seller seller, SellerEntity entity) {
        entity.setIdentification(seller.getIdentification());
        entity.setFullName(seller.getFullName());
        entity.setEmail(seller.getEmail());
        entity.setPhoneNumber(seller.getPhoneNumber());
        entity.setAddress(seller.getAddress());
        entity.setRole(code(seller.getRole()));
        entity.setBusinessName(seller.getBusinessName());
        entity.setRegistrationDate(seller.getRegistrationDate());
        entity.setStatus(code(seller.getStatus()));
    }

    // ------------------------------------------------------------------ User

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setUserId(entity.getUserId());
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setIdentification(entity.getIdentification());
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setPhoneNumber(entity.getPhoneNumber());
        user.setAddress(entity.getAddress());
        user.setRole(fromCode(SystemRole.class, entity.getRole()));
        user.setStatus(fromCode(UserStatus.class, entity.getStatus()));
        if (entity.getBuyer() != null) {
            user.setParticipant(toDomain(entity.getBuyer()));
        } else if (entity.getSeller() != null) {
            user.setParticipant(toDomain(entity.getSeller()));
        }
        return user;
    }

    /** Scalar attributes; the participant foreign keys are resolved by UserMySqlAdapter. */
    public static void copy(User user, UserEntity entity) {
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        copyPerson(user, entity);
        entity.setStatus(code(user.getStatus()));
    }

    private static void copyPerson(Person person, UserEntity entity) {
        entity.setIdentification(person.getIdentification());
        entity.setFullName(person.getFullName());
        entity.setEmail(person.getEmail());
        entity.setPhoneNumber(person.getPhoneNumber());
        entity.setAddress(person.getAddress());
        entity.setRole(code(person.getRole()));
    }
}
