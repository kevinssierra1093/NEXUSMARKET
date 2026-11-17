package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.requests.RegisterParticipantUserRequest;
import application.adapters.in.rest.requests.RegisterSellerRequest;
import application.adapters.in.rest.requests.RegisterStaffUserRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.requests.UpdateSellerRequest;
import application.adapters.in.rest.requests.WarehouseDataRequest;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.SellerResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.Buyer;
import application.domain.models.MarketplaceParticipant;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.SystemRole;

import java.util.ArrayList;
import java.util.List;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Users, buyers and sellers: Request DTO -> Domain Model and Domain Model -> Response DTO. */
public final class ParticipantRestMapper {

    private ParticipantRestMapper() {
    }

    // ------------------------------------------------------------- requests

    public static Buyer toBuyer(RegisterBuyerRequest request) {
        Buyer buyer = new Buyer();
        buyer.setIdentification(request.identification());
        buyer.setFullName(request.fullName());
        buyer.setEmail(request.email());
        buyer.setPhoneNumber(request.phoneNumber());
        buyer.setAddress(request.address());
        buyer.setPrimaryAddress(request.primaryAddress());
        buyer.setAdditionalAddresses(copy(request.additionalAddresses()));
        return buyer;
    }

    /** System access of a self-registered buyer. */
    public static User toBuyerUser(RegisterBuyerRequest request, Buyer buyer) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setIdentification(buyer.getIdentification());
        user.setFullName(buyer.getFullName());
        user.setEmail(buyer.getEmail());
        user.setPhoneNumber(buyer.getPhoneNumber());
        user.setAddress(buyer.getAddress());
        user.setRole(SystemRole.BUYER);
        user.setParticipant(buyer);
        return user;
    }

    public static User toStaffUser(RegisterStaffUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setIdentification(request.identification());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setRole(fromCode(SystemRole.class, request.role()));
        return user;
    }

    /** The participant is referenced by the identification; the domain validates role/participant match. */
    public static User toParticipantUser(RegisterParticipantUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setIdentification(request.identification());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        SystemRole role = fromCode(SystemRole.class, request.role());
        user.setRole(role);
        if (SystemRole.BUYER.equals(role)) {
            user.setParticipant(RestReferences.buyer(request.identification()));
        } else if (SystemRole.SELLER.equals(role)) {
            user.setParticipant(RestReferences.seller(request.identification()));
        }
        return user;
    }

    public static Buyer toBuyer(String identification, UpdateBuyerRequest request) {
        Buyer buyer = RestReferences.buyer(identification);
        buyer.setFullName(request.fullName());
        buyer.setPhoneNumber(request.phoneNumber());
        buyer.setAddress(request.address());
        buyer.setPrimaryAddress(request.primaryAddress());
        buyer.setAdditionalAddresses(copy(request.additionalAddresses()));
        return buyer;
    }

    public static Seller toSeller(RegisterSellerRequest request) {
        Seller seller = new Seller();
        seller.setIdentification(request.identification());
        seller.setFullName(request.fullName());
        seller.setEmail(request.email());
        seller.setPhoneNumber(request.phoneNumber());
        seller.setAddress(request.address());
        seller.setBusinessName(request.businessName());
        return seller;
    }

    public static Warehouse toWarehouse(WarehouseDataRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        return warehouse;
    }

    public static Seller toSeller(String identification, UpdateSellerRequest request) {
        Seller seller = RestReferences.seller(identification);
        seller.setFullName(request.fullName());
        seller.setPhoneNumber(request.phoneNumber());
        seller.setAddress(request.address());
        seller.setBusinessName(request.businessName());
        return seller;
    }

    // ------------------------------------------------------------ responses

    public static UserResponse toResponse(User user) {
        MarketplaceParticipant participant = user.getParticipant();
        String participantType = participant instanceof Buyer ? "BUYER" : participant instanceof Seller ? "SELLER" : null;
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getIdentification(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                code(user.getRole()),
                code(user.getStatus()),
                participantType,
                participant == null ? null : participant.getIdentification());
    }

    public static BuyerResponse toResponse(Buyer buyer) {
        return new BuyerResponse(
                buyer.getIdentification(),
                buyer.getFullName(),
                buyer.getEmail(),
                buyer.getPhoneNumber(),
                buyer.getAddress(),
                buyer.getPrimaryAddress(),
                buyer.getAdditionalAddresses() == null ? List.of() : List.copyOf(buyer.getAdditionalAddresses()),
                code(buyer.getStatus()));
    }

    public static SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
                seller.getIdentification(),
                seller.getFullName(),
                seller.getEmail(),
                seller.getPhoneNumber(),
                seller.getAddress(),
                seller.getBusinessName(),
                seller.getRegistrationDate(),
                code(seller.getStatus()),
                seller.getWarehouses() == null ? List.of()
                        : seller.getWarehouses().stream().map(CatalogRestMapper::toResponse).toList(),
                seller.getProducts() == null ? List.of()
                        : seller.getProducts().stream().map(CatalogRestMapper::toResponse).toList());
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
