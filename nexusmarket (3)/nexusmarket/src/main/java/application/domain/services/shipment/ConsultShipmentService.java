package application.domain.services.shipment;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.services.authorization.AuthorizeOrderConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultShipmentService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeOrderConsultationService authorizeOrderConsultationService;

    public List<Shipment> executeByOrder(User requestingUser, Order order) {
        Order stored = orderRepositoryPort.findByIdentifier(order)
                .orElseThrow(() -> new EntityNotFoundException("Order"));
        authorizeOrderConsultationService.execute(requestingUser, stored);
        return shipmentRepositoryPort.findByOrder(stored);
    }
}
