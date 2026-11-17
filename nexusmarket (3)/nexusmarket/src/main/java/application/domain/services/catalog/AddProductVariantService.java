package application.domain.services.catalog;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.services.authorization.AuthorizeProductOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddProductVariantService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public ProductVariant execute(User requestingUser, Product product, ProductVariant variant) {
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        authorizeProductOperationService.execute(requestingUser, stored);
        if (ProductStatus.DISCONTINUED.equals(stored.getStatus())) {
            throw new DomainException("Variants cannot be added to a discontinued product.");
        }
        if (variant.getPrice() == null || variant.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("The variant must have a price greater than zero.");
        }
        variant.setProduct(stored);
        ProductVariant saved = productVariantRepositoryPort.save(variant);
        Map<String, Object> details = new HashMap<>();
        details.put("variantIdentifier", saved.getIdentifier());
        details.put("attributes", saved.getAttributes());
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_UPDATE, stored, details);
        return saved;
    }
}
