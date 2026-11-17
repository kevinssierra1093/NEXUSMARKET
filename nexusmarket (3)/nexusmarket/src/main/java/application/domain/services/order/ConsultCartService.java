package application.domain.services.order;

import application.domain.models.Order;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConsultCartService {

    private final ResolveBuyerCartService resolveBuyerCartService;
    private final CalculateOrderTotalService calculateOrderTotalService;

    public Order execute(User requestingUser) {
        return resolveBuyerCartService.find(requestingUser);
    }

    public BigDecimal executeTotal(User requestingUser) {
        return calculateOrderTotalService.execute(resolveBuyerCartService.find(requestingUser));
    }
}
