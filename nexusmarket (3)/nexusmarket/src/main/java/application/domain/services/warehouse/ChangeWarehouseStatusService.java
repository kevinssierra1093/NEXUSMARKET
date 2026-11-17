package application.domain.services.warehouse;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChangeWarehouseStatusService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Warehouse execute(User requestingUser, Warehouse warehouse, WarehouseStatus newStatus) {
        authorizeAdministrationService.execute(requestingUser);
        Warehouse stored = warehouseRepositoryPort.findByIdentifier(warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        if (stored.getStatus().equals(newStatus)) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), newStatus.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(newStatus);
        warehouseRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", newStatus.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.WAREHOUSE_UPDATE, stored, details);
        return stored;
    }
}
