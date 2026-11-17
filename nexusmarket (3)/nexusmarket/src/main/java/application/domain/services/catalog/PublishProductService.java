package application.domain.services.catalog;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.services.authorization.AuthorizeProductOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.SellerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublishProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Product execute(User requestingUser, Product product) {
        Product stored = productRepositoryPort.findByIdentifier(product)
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        authorizeProductOperationService.execute(requestingUser, stored);
        if (!ProductStatus.SUSPENDED.equals(stored.getStatus())) {
            throw new InvalidStatusTransitionException(stored.getStatus().getCode(), ProductStatus.PUBLISHED.getCode());
        }
        if (!SellerStatus.ACTIVE.equals(stored.getSeller().getStatus())) {
            throw new DomainException("The seller must be active to publish products.");
        }
        List<ProductVariant> variants = productVariantRepositoryPort.findByProduct(stored);
        if (variants.isEmpty()) {
            throw new DomainException("A product without variants cannot be published.");
        }
        if (ProductType.PHYSICAL.equals(stored.getType())) {
            validateInitialStock(variants);
        }
        stored.setStatus(ProductStatus.PUBLISHED);
        productRepositoryPort.update(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", ProductStatus.SUSPENDED.getCode());
        details.put("newStatus", ProductStatus.PUBLISHED.getCode());
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_PUBLICATION, stored, details);
        return stored;
    }

    // Physical products need sellable stock in at least one warehouse before being visible.
    private void validateInitialStock(List<ProductVariant> variants) {
        boolean hasStock = variants.stream()
                .flatMap(variant -> inventoryItemRepositoryPort.findByVariant(variant).stream())
                .anyMatch(this::isSellable);
        if (!hasStock) {
            throw new DomainException("A physical product requires available inventory before being published.");
        }
    }

    private boolean isSellable(InventoryItem item) {
        return InventoryItemStatus.AVAILABLE.equals(item.getStatus()) && item.getQuantityAvailable() > 0;
    }
}
