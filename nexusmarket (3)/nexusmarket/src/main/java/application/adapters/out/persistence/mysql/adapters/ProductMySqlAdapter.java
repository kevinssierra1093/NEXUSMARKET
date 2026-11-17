package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.entities.ProductEntity;
import application.adapters.out.persistence.mysql.mappers.CatalogPersistenceMapper;
import application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static application.adapters.shared.ValueObjectResolver.code;

/** Persists only the product; variants are persisted through ProductVariantMySqlAdapter. */
@Component
@Transactional
@RequiredArgsConstructor
public class ProductMySqlAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;
    private final JpaReferences references;

    @Override
    public Product save(Product product) {
        if (product.getIdentifier() == null) {
            product.setIdentifier(JpaReferences.newIdentifier());
        }
        ProductEntity entity = new ProductEntity();
        entity.setIdentifier(product.getIdentifier());
        apply(product, entity);
        productJpaRepository.save(entity);
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByIdentifier(Product product) {
        if (product == null || product.getIdentifier() == null) {
            return Optional.empty();
        }
        return productJpaRepository.findById(product.getIdentifier()).map(CatalogPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findBySeller(Seller seller) {
        if (seller == null || seller.getIdentification() == null) {
            return List.of();
        }
        return productJpaRepository.findBySeller_Identification(seller.getIdentification()).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByStatus(Product product) {
        return productJpaRepository.findByStatus(code(product.getStatus())).stream()
                .map(CatalogPersistenceMapper::toDomain).toList();
    }

    @Override
    public void update(Product product) {
        ProductEntity entity = productJpaRepository.findById(product.getIdentifier())
                .orElseThrow(() -> new EntityNotFoundException("Product"));
        apply(product, entity);
        productJpaRepository.save(entity);
    }

    private void apply(Product product, ProductEntity entity) {
        CatalogPersistenceMapper.copy(product, entity);
        entity.setSeller(references.seller(product.getSeller()));
    }
}
