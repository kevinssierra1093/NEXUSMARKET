package application.domain.services.seller;

import application.domain.exceptions.DomainException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SellerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.WarehouseStatus;
import application.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Sellers cannot self-register: the administrator incorporates the seller together with
 * the seller's first warehouse (business flow, step 1 "Incorporación").
 */
@Service
@RequiredArgsConstructor
public class RegisterSellerService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Seller execute(User requestingUser, Seller seller, Warehouse firstWarehouse) {
        authorizeAdministrationService.execute(requestingUser);
        validateRequiredData(seller, firstWarehouse);
        validateUniqueness(seller);

        seller.setRole(SystemRole.SELLER);
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setRegistrationDate(LocalDate.now());
        Seller savedSeller = sellerRepositoryPort.save(seller);

        firstWarehouse.setType(WarehouseType.SELLER);
        firstWarehouse.setOwner(savedSeller);
        firstWarehouse.setStatus(WarehouseStatus.ACTIVE);
        Warehouse savedWarehouse = warehouseRepositoryPort.save(firstWarehouse);
        savedSeller.getWarehouses().add(savedWarehouse);

        Map<String, Object> sellerDetails = new HashMap<>();
        sellerDetails.put("sellerIdentification", savedSeller.getIdentification());
        sellerDetails.put("businessName", savedSeller.getBusinessName());
        registerOperationAndAuditService.execute(requestingUser, OperationType.SELLER_REGISTRATION, null, sellerDetails);

        Map<String, Object> warehouseDetails = new HashMap<>();
        warehouseDetails.put("sellerIdentification", savedSeller.getIdentification());
        warehouseDetails.put("name", savedWarehouse.getName());
        registerOperationAndAuditService.execute(requestingUser, OperationType.WAREHOUSE_REGISTRATION,
                savedWarehouse, warehouseDetails);
        return savedSeller;
    }

    private void validateRequiredData(Seller seller, Warehouse firstWarehouse) {
        if (seller.getFullName() == null || seller.getFullName().isBlank()) {
            throw new DomainException("The seller full name must not be empty.");
        }
        if (seller.getBusinessName() == null || seller.getBusinessName().isBlank()) {
            throw new DomainException("The seller business name is required.");
        }
        if (firstWarehouse == null) {
            throw new DomainException("A seller must be registered together with its first warehouse.");
        }
    }

    // Identification and email must be unique across the whole platform.
    private void validateUniqueness(Seller seller) {
        if (sellerRepositoryPort.existsByIdentification(seller) || buyerRepositoryPort.existsByIdentification(seller)
                || userRepositoryPort.existsByIdentification(seller)) {
            throw new DomainException("A person with identification " + seller.getIdentification() + " already exists.");
        }
        if (sellerRepositoryPort.existsByEmail(seller) || buyerRepositoryPort.existsByEmail(seller)
                || userRepositoryPort.existsByEmail(seller)) {
            throw new DomainException("A person with email " + seller.getEmail() + " already exists.");
        }
    }
}
