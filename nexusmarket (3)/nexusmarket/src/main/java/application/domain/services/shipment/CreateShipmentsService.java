package application.domain.services.shipment;

import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal domain service invoked when an order is paid: creates one PREPARING shipment per
 * origin warehouse of the order's physical items.
 */
@Service
@RequiredArgsConstructor
public class CreateShipmentsService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public List<Shipment> execute(User requestingUser, Order order) {
        Map<String, Warehouse> origins = new LinkedHashMap<>();
        for (OrderItem line : order.getItems()) {
            if (line.getWarehouse() != null) {
                origins.putIfAbsent(line.getWarehouse().getIdentifier(), line.getWarehouse());
            }
        }
        List<Shipment> created = new ArrayList<>();
        for (Warehouse origin : origins.values()) {
            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setOriginWarehouse(origin);
            shipment.setStatus(ShipmentStatus.PREPARING);
            Shipment saved = shipmentRepositoryPort.save(shipment);
            created.add(saved);
            Map<String, Object> details = new HashMap<>();
            details.put("event", "CREATED");
            details.put("order", order.getIdentifier());
            details.put("originWarehouse", origin.getIdentifier());
            details.put("newStatus", ShipmentStatus.PREPARING.getCode());
            registerOperationAndAuditService.execute(requestingUser, OperationType.SHIPMENT_UPDATE, saved, details);
        }
        return created;
    }
}
