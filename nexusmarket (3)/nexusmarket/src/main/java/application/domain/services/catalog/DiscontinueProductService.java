package application.domain.services.catalog;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.authorization.AuthorizeProductOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** DISCONTINUED is a terminal status: a discontinued product can never be reactivated. */
@Service
@RequiredArgsConstructor
public class DiscontinueProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Product execute(User requestingUser, Product product) {
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        authorizeProductOperationService.execute(requestingUser, stored);
        if (ProductStatus.DISCONTINUED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(),
                    ProductStatus.DISCONTINUED.getCode());
        }
        String previous = stored.getStatus().getCode();
        stored.setStatus(ProductStatus.DISCONTINUED);
        productRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previous);
        details.put("newStatus", ProductStatus.DISCONTINUED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_DISCONTINUATION, stored, details);
        return stored;
    }
}
