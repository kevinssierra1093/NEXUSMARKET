package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CatalogRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.RegisterProductRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.requests.VariantRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.VariantResponse;
import application.domain.services.catalog.AddProductVariantService;
import application.domain.services.catalog.ConsultProductService;
import application.domain.services.catalog.ConsultPublishedCatalogService;
import application.domain.services.catalog.DiscontinueProductService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.catalog.RegisterProductService;
import application.domain.services.catalog.SuspendProductService;
import application.domain.services.catalog.UpdateProductService;
import application.domain.services.inventory.ConsultInventoryService;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Transactional
public class ProductController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RegisterProductService registerProductService;
    private final AddProductVariantService addProductVariantService;
    private final UpdateProductService updateProductService;
    private final PublishProductService publishProductService;
    private final SuspendProductService suspendProductService;
    private final DiscontinueProductService discontinueProductService;
    private final ConsultProductService consultProductService;
    private final ConsultPublishedCatalogService consultPublishedCatalogService;
    private final ConsultInventoryService consultInventoryService;

    /** SELLER: the product starts SUSPENDED until it is published. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse register(@Valid @RequestBody RegisterProductRequest request) {
        return CatalogRestMapper.toResponse(
                registerProductService.execute(authenticatedUser.resolve(), CatalogRestMapper.toProduct(request)));
    }

    /** Public catalog: PUBLISHED products. */
    @GetMapping
    public List<ProductResponse> publishedCatalog() {
        return consultPublishedCatalogService.execute(authenticatedUser.resolve()).stream()
                .map(CatalogRestMapper::toResponse).toList();
    }

    @GetMapping("/{identifier}")
    public ProductResponse consult(@PathVariable String identifier) {
        return CatalogRestMapper.toResponse(
                consultProductService.execute(authenticatedUser.resolve(), RestReferences.product(identifier)));
    }

    @PutMapping("/{identifier}")
    public ProductResponse update(@PathVariable String identifier, @Valid @RequestBody UpdateProductRequest request) {
        return CatalogRestMapper.toResponse(updateProductService.execute(authenticatedUser.resolve(),
                CatalogRestMapper.toProduct(identifier, request)));
    }

    @PostMapping("/{identifier}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public VariantResponse addVariant(@PathVariable String identifier, @Valid @RequestBody VariantRequest request) {
        return CatalogRestMapper.toResponse(addProductVariantService.execute(authenticatedUser.resolve(),
                RestReferences.product(identifier), CatalogRestMapper.toVariant(request)));
    }

    @PostMapping("/{identifier}/publish")
    public ProductResponse publish(@PathVariable String identifier) {
        return CatalogRestMapper.toResponse(
                publishProductService.execute(authenticatedUser.resolve(), RestReferences.product(identifier)));
    }

    @PostMapping("/{identifier}/suspend")
    public ProductResponse suspend(@PathVariable String identifier) {
        return CatalogRestMapper.toResponse(
                suspendProductService.execute(authenticatedUser.resolve(), RestReferences.product(identifier)));
    }

    @PostMapping("/{identifier}/discontinue")
    public ProductResponse discontinue(@PathVariable String identifier) {
        return CatalogRestMapper.toResponse(
                discontinueProductService.execute(authenticatedUser.resolve(), RestReferences.product(identifier)));
    }

    @GetMapping("/variants/{variantIdentifier}/inventory")
    public List<InventoryItemResponse> variantInventory(@PathVariable String variantIdentifier) {
        return consultInventoryService.executeByVariant(authenticatedUser.resolve(),
                        RestReferences.variant(variantIdentifier))
                .stream().map(CatalogRestMapper::toResponse).toList();
    }
}
