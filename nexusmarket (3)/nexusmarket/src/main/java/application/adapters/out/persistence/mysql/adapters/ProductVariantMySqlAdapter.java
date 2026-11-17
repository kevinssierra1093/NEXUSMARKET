package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import application.adapters.out.persistence.mysql.mappers.CatalogPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.ProductVariantJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.ports.out.ProductVariantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class ProductVariantMySqlAdapter implements ProductVariantRepositoryPort {

    private final ProductVariantJpaRepository productVariantJpaRepository;
    private final JpaReferences references;

    @Override
    public ProductVariant save(ProductVariant variant) {
        if (variant.getIdentifier() == null) {
            variant.setIdentifier(JpaReferences.newIdentifier());
        }
        ProductVariantEntity entity = new ProductVariantEntity();
        entity.setIdentifier(variant.getIdentifier());
        apply(variant, entity);
        productVariantJpaRepository.save(entity);
        return variant;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductVariant> findByIdentifier(ProductVariant variant) {
        if (variant == null || variant.getIdentifier() == null) {
            return Optional.empty();
        }
        return productVariantJpaRepository.findById(variant.getIdentifier()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> findByProduct(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return List.of();
        }
        return productVariantJpaRepository.findByProduct_Identifier(product.getIdentifier()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(ProductVariant variant) {
        ProductVariantEntity entity = productVariantJpaRepository.findById(variant.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant"));
        apply(variant, entity);
        productVariantJpaRepository.save(entity);
    }

    private void apply(ProductVariant variant, ProductVariantEntity entity) {
        CatalogPersistenceMapper.copy(variant, entity);
        entity.setProduct(references.product(variant.getProduct()));
    }
}
