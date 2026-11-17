package application.domain.services.catalog;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
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
public class UpdateProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Product execute(User requestingUser, Product product) {
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        authorizeProductOperationService.execute(requestingUser, stored);
        if (ProductStatus.DISCONTINUED.equals(stored.getStatus())) {
            throw new DomainException("A discontinued product cannot be updated.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new DomainException("The product name must not be empty.");
        }
        Map<String, Object> details = new HashMap<>();
        details.put("previousName", stored.getName());
        details.put("previousDescription", stored.getDescription());
        stored.setName(product.getName());
        stored.setDescription(product.getDescription());
        productRepositoryPort.update(stored);
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_UPDATE, stored, details);
        return stored;
    }
}
