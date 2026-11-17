package application.domain.services.order;

import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal domain service: closes the order after the delivery is confirmed (or right after
 * payment for digital-only orders). A finalized order can never be modified again.
 */
@Service
@RequiredArgsConstructor
public class FinalizeOrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Order execute(User requestingUser, Order order) {
        boolean finalizable = OrderStatus.PAID.equals(order.getOrderStatus())
                || OrderStatus.SHIPPED.equals(order.getOrderStatus());
        if (!finalizable) {
            throw new InvalidStatusTransitionException(order.getOrderStatus().getCode(),
                    OrderStatus.FINALIZED.getCode());
        }
        String previous = order.getOrderStatus().getCode();
        order.setOrderStatus(OrderStatus.FINALIZED);
        order.setDeliveryDate(LocalDateTime.now());
        orderRepositoryPort.update(order);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", OrderStatus.FINALIZED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.ORDER_FINALIZATION, order, details);
        return order;
    }
}
