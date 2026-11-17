package application.domain.services.inventory;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.services.authorization.AuthorizeInventoryConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultInventoryMovementsService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
    private final AuthorizeInventoryConsultationService authorizeInventoryConsultationService;

    public List<InventoryMovement> execute(User requestingUser, InventoryItem item) {
        InventoryItem stored = inventoryItemRepositoryPort.findByIdentifier(item)
                .orElseThrow(() -> new EntityNotFoundException("InventoryItem"));
        authorizeInventoryConsultationService.execute(requestingUser, stored.getVariant());
        return inventoryMovementRepositoryPort.findByInventoryItem(stored);
    }
}
