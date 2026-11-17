package application.domain.services.warehouse;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdateWarehouseService {

    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final AuthorizeAdministrationService authorizeAdministrationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Warehouse execute(User requestingUser, Warehouse warehouse) {
        authorizeAdministrationService.execute(requestingUser);
        Warehouse stored = warehouseRepositoryPort.findByIdentifier(warehouse)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse"));
        if (warehouse.getName() == null || warehouse.getName().isBlank()
                || warehouse.getAddress() == null || warehouse.getAddress().isBlank()) {
            throw new DomainException("The warehouse name and address must not be empty.");
        }
        Map<String, Object> details = new HashMap<>();
        details.put("previousName", stored.getName());
        details.put("previousAddress", stored.getAddress());
        stored.setName(warehouse.getName());
        stored.setAddress(warehouse.getAddress());
        warehouseRepositoryPort.update(stored);
        registerOperationAndAuditService.execute(requestingUser, OperationType.WAREHOUSE_UPDATE, stored, details);
        return stored;
    }
}
