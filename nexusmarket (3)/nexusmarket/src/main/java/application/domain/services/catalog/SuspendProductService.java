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

@Service
@RequiredArgsConstructor
public class SuspendProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Product execute(User requestingUser, Product product) {
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        authorizeProductOperationService.execute(requestingUser, stored);
        if (!ProductStatus.PUBLISHED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), ProductStatus.SUSPENDED.getCode());
        }
        stored.setStatus(ProductStatus.SUSPENDED);
        productRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", ProductStatus.PUBLISHED.getCode());
        details.put("newStatus", ProductStatus.SUSPENDED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_SUSPENSION, stored, details);
        return stored;
    }
}
