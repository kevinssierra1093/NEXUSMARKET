package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.InventoryInboundRequest;
import application.adapters.in.rest.requests.RegisterProductRequest;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.requests.VariantRequest;
import application.adapters.in.rest.requests.WarehouseDataRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.VariantResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Warehouse;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.WarehouseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static application.adapters.shared.ValueObjectResolver.code;
import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Warehouses, products, variants and inventory: Request DTO <-> Domain Model <-> Response DTO. */
public final class CatalogRestMapper {

    private CatalogRestMapper() {
    }

    // ------------------------------------------------------------- requests

    public static Warehouse toWarehouse(RegisterWarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setType(fromCode(WarehouseType.class, request.type()));
        if (request.ownerIdentification() != null && !request.ownerIdentification().isBlank()) {
            warehouse.setOwner(RestReferences.seller(request.ownerIdentification()));
        }
        return warehouse;
    }

    public static Warehouse toWarehouse(String identifier, WarehouseDataRequest request) {
        Warehouse warehouse = RestReferences.warehouse(identifier);
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        return warehouse;
    }

    /** The seller is resolved by the domain from the authenticated user. */
    public static Product toProduct(RegisterProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setType(fromCode(ProductType.class, request.type()));
        product.setVariants(request.variants().stream().map(CatalogRestMapper::toVariant)
                .collect(Collectors.toCollection(ArrayList::new)));
        return product;
    }

    public static ProductVariant toVariant(VariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setAttributes(request.attributes() == null ? new HashMap<>() : new HashMap<>(request.attributes()));
        variant.setPrice(request.price());
        return variant;
    }

    public static Product toProduct(String identifier, UpdateProductRequest request) {
        Product product = RestReferences.product(identifier);
        product.setName(request.name());
        product.setDescription(request.description());
        return product;
    }

    public static InventoryItem toInventoryItem(InventoryInboundRequest request) {
        InventoryItem item = new InventoryItem();
        item.setVariant(RestReferences.variant(request.variantIdentifier()));
        item.setWarehouse(RestReferences.warehouse(request.warehouseIdentifier()));
        return item;
    }

    // ------------------------------------------------------------ responses

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getIdentifier(),
                warehouse.getName(),
                warehouse.getAddress(),
                code(warehouse.getType()),
                warehouse.getOwner() == null ? null : warehouse.getOwner().getIdentification(),
                warehouse.getOwner() == null ? null : warehouse.getOwner().getBusinessName(),
                code(warehouse.getStatus()));
    }

    public static ProductResponse toResponse(Product product) {
        List<VariantResponse> variants = product.getVariants() == null ? List.of()
                : product.getVariants().stream().map(CatalogRestMapper::toResponse).toList();
        return new ProductResponse(
                product.getIdentifier(),
                product.getSeller() == null ? null : product.getSeller().getIdentification(),
                product.getSeller() == null ? null : product.getSeller().getBusinessName(),
                product.getName(),
                product.getDescription(),
                code(product.getType()),
                code(product.getStatus()),
                variants);
    }

    public static VariantResponse toResponse(ProductVariant variant) {
        return new VariantResponse(
                variant.getIdentifier(),
                variant.getProduct() == null ? null : variant.getProduct().getIdentifier(),
                attributes(variant),
                variant.getPrice());
    }

    public static InventoryItemResponse toResponse(InventoryItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant == null ? null : variant.getProduct();
        return new InventoryItemResponse(
                item.getIdentifier(),
                variant == null ? null : variant.getIdentifier(),
                attributes(variant),
                product == null ? null : product.getIdentifier(),
                product == null ? null : product.getName(),
                item.getWarehouse() == null ? null : item.getWarehouse().getIdentifier(),
                item.getWarehouse() == null ? null : item.getWarehouse().getName(),
                item.getQuantityAvailable(),
                item.getQuantityReserved(),
                code(item.getStatus()));
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getMovementId(),
                movement.getInventoryItem() == null ? null : movement.getInventoryItem().getIdentifier(),
                code(movement.getMovementType()),
                movement.getQuantity(),
                movement.getMovementDate(),
                movement.getPerformedBy() == null ? null : movement.getPerformedBy().getUsername(),
                movement.getRelatedOrder() == null ? null : movement.getRelatedOrder().getIdentifier());
    }

    static Map<String, String> attributes(ProductVariant variant) {
        return variant == null || variant.getAttributes() == null ? Map.of()
                : new java.util.TreeMap<>(variant.getAttributes());
    }
}
