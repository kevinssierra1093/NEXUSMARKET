package application.domain.ports.out;

import application.domain.models.Product;
import application.domain.models.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepositoryPort {

    ProductVariant save(ProductVariant variant);

    Optional<ProductVariant> findByIdentifier(ProductVariant variant);

    List<ProductVariant> findByProduct(Product product);

    void update(ProductVariant variant);
}
