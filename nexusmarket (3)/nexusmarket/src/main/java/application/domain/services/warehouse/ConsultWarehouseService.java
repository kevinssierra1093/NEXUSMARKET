package application.domain.services.warehouse;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.ValidateRoleAuthorizationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConsultWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    public Warehouse execute(User requestingUser, Warehouse warehouse) {
        authorizeConsultation(requestingUser);
        Warehouse stored = warehouseRepositoryPort.findByIdentifier(warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        if (SystemRole.SELLER.equals(requestingUser.getRole())) {
            validateSellerAccess(requestingUser, stored);
        }
        return stored;
    }

    public List<Warehouse> executeBySeller(User requestingUser, Seller seller) {
        authorizeConsultation(requestingUser);
        if (SystemRole.SELLER.equals(requestingUser.getRole())
                && !Objects.equals(requestingUser.getParticipant().getIdentification(), seller.getIdentification())) {
            throw new UnauthorizedOperationException("A seller can only consult their own warehouses.");
        }
        return warehouseRepositoryPort.findBySeller(seller);
    }

    public List<Warehouse> executeAll(User requestingUser) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR, SystemRole.LOGISTICS_OPERATOR);
        return warehouseRepositoryPort.findAll();
    }

    private void authorizeConsultation(User user) {
        validateUserAuthorizationStatusService.execute(user);
        validateRoleAuthorizationService.execute(user, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR,
                SystemRole.LOGISTICS_OPERATOR, SystemRole.SELLER);
    }

    private void validateSellerAccess(User user, Warehouse warehouse) {
        boolean own = warehouse.getOwner() != null && user.getParticipant() != null
                && Objects.equals(user.getParticipant().getIdentification(), warehouse.getOwner().getIdentification());
        if (!own) {
            throw new UnauthorizedOperationException("A seller can only consult their own warehouses.");
        }
    }
}
