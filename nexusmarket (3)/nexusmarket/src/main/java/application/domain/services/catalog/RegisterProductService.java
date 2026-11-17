package application.domain.services.catalog;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ProductVariantRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.AuthorizeProductOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SellerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers a product with its variants. The product starts SUSPENDED (not visible); it is
 * published later, once its initial stock exists (business flow, steps 2 to 4).
 */
@Service
@RequiredArgsConstructor
public class RegisterProductService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductVariantRepositoryPort productVariantRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final AuthorizeProductOperationService authorizeProductOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Product execute(User requestingUser, Product product) {
        resolveSeller(requestingUser, product);
        authorizeProductOperationService.execute(requestingUser, product);
        Seller seller = sellerRepositoryPort.findByIdentification(product.getSeller())
                .orElseThrow(() -> new EntityNotFoundException("Product seller"));
        if (!SellerStatus.ACTIVE.equals(seller.getStatus())) {
            throw new DomainException("The seller must be active to register products.");
        }
        validateRequiredData(product);

        List<ProductVariant> variants = new ArrayList<>(product.getVariants());
        product.setSeller(seller);
        product.setStatus(ProductStatus.SUSPENDED);
        product.setVariants(new ArrayList<>());
        Product saved = productRepositoryPort.save(product);

        for (ProductVariant variant : variants) {
            variant.setProduct(saved);
            saved.getVariants().add(productVariantRepositoryPort.save(variant));
        }

        Map<String, Object> details = new HashMap<>();
        details.put("name", saved.getName());
        details.put("type", saved.getType().getCode());
        details.put("variants", saved.getVariants().size());
        registerOperationAndAuditService.execute(requestingUser, OperationType.PRODUCT_REGISTRATION, saved, details);
        return saved;
    }

    private void resolveSeller(User user, Product product) {
        if (product.getSeller() == null && user.getParticipant() instanceof Seller seller) {
            product.setSeller(seller);
        }
        if (product.getSeller() == null) {
            throw new UnauthorizedOperationException("Only a seller can register products.");
        }
    }

    private void validateRequiredData(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new DomainException("The product name must not be empty.");
        }
        if (product.getType() == null) {
            throw new DomainException("The product type (PHYSICAL or DIGITAL) is required.");
        }
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw new DomainException("A product must define at least one variant.");
        }
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getPrice() == null || variant.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("Every variant must have a price greater than zero.");
            }
        }
    }
}
