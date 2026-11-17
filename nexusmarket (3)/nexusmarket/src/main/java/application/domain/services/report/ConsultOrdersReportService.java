package application.domain.services.report;

import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.order.CalculateOrderTotalService;
import application.domain.valueobjects.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultOrdersReportService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;
    private final CalculateOrderTotalService calculateOrderTotalService;

    public Map<String, Object> execute(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        List<Order> orders = orderRepositoryPort.findAll();
        Map<String, Long> ordersByStatus = orders.stream().collect(Collectors.groupingBy(
                order -> order.getOrderStatus().getCode(), TreeMap::new, Collectors.counting()));
        BigDecimal totalSales = orders.stream()
                .filter(this::isSold)
                .map(calculateOrderTotalService::execute)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalOrders", orders.size());
        report.put("ordersByStatus", ordersByStatus);
        report.put("totalSales", totalSales);
        return report;
    }

    // A sale exists once the payment has been confirmed.
    private boolean isSold(Order order) {
        return OrderStatus.PAID.equals(order.getOrderStatus())
                || OrderStatus.SHIPPED.equals(order.getOrderStatus())
                || OrderStatus.FINALIZED.equals(order.getOrderStatus());
    }
}
