package application.domain.services.returns;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.ProductVariant;
import org.springframework.stereotype.Service;

/**
 * Internal domain service: finds the order line that corresponds to the product (variant) of a
 * return. A variant appears at most once per order.
 */
@Service
public class ResolveReturnLineService {

    public OrderItem execute(Order order, ProductVariant variant) {
        if (variant == null || variant.getIdentifier() == null) {
            throw new DomainException("The product (variant) to return is required.");
        }
        return order.getItems().stream()
                .filter(line -> line.getVariant().getIdentifier().equals(variant.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Order item for variant " + variant.getIdentifier()));
    }
}
