package application.domain.services.report;

import application.domain.models.InventoryItem;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.valueobjects.InventoryItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultInventoryReportService {

    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public Map<String, Object> execute(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        List<InventoryItem> items = inventoryItemRepositoryPort.findAll();
        int totalAvailable = items.stream().mapToInt(InventoryItem::getQuantityAvailable).sum();
        int totalReserved = items.stream().mapToInt(InventoryItem::getQuantityReserved).sum();
        long damagedRecords = items.stream()
                .filter(item -> InventoryItemStatus.DAMAGED.equals(item.getStatus())).count();
        Map<String, Integer> availableByWarehouse = items.stream().collect(Collectors.groupingBy(
                item -> item.getWarehouse().getIdentifier(), TreeMap::new,
                Collectors.summingInt(InventoryItem::getQuantityAvailable)));
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("inventoryRecords", items.size());
        report.put("totalAvailableUnits", totalAvailable);
        report.put("totalReservedUnits", totalReserved);
        report.put("damagedRecords", damagedRecords);
        report.put("availableUnitsByWarehouse", availableByWarehouse);
        return report;
    }
}
