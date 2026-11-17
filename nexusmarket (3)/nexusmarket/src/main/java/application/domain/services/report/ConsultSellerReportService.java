package application.domain.services.report;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.InventoryItemRepositoryPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.ValidateSellerOwnershipService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.InventoryItemStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductType;
import application.domain.valueobjects.RefundStatus;
import application.domain.valueobjects.ReturnStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Report of ONE seller: products, sales, available inventory, stock movements caused by
 * purchases / returns / adjustments, and returns and refunds of the seller's products.
 *
 * The report is computed only from persisted data and contains no timestamps, so consulting it
 * before and after restarting the application must give exactly the same result.
 *
 * Authorization: the owning SELLER, or ADMINISTRATOR / SUPERVISOR (any seller).
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerReportService {

    private static final Set<OrderStatus> SOLD_STATUSES = Set.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.FINALIZED);

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InventoryItemRepositoryPort inventoryItemRepositoryPort;
    private final InventoryMovementRepositoryPort inventoryMovementRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public Map<String, Object> execute(User requestingUser, Seller seller) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        if (SystemRole.SELLER.equals(requestingUser.getRole())) {
            validateSellerOwnershipService.execute(requestingUser, seller);
        } else {
            authorizeAdministrativeConsultationService.execute(requestingUser);
        }
        Seller stored = sellerRepositoryPort.findByIdentification(seller)
                .orElseThrow(() -> new EntityNotFoundException("Seller"));
        return build(stored);
    }

    /** ADMINISTRATOR / SUPERVISOR: the report of every seller, ordered by identification. */
    public List<Map<String, Object>> executeAll(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return sellerRepositoryPort.findAll().stream()
                .sorted(Comparator.comparing(Seller::getIdentification))
                .map(this::build)
                .toList();
    }

    private Map<String, Object> build(Seller seller) {
        String sellerId = seller.getIdentification();
        Map<String, Object> report = new LinkedHashMap<>();

        Map<String, Object> sellerInfo = new LinkedHashMap<>();
        sellerInfo.put("identification", sellerId);
        sellerInfo.put("businessName", seller.getBusinessName());
        sellerInfo.put("status", seller.getStatus().getCode());
        report.put("seller", sellerInfo);

        report.put("products", products(seller));
        Map<String, Object> sales = sales(sellerId);
        report.put("sales", sales);
        report.put("inventory", inventory(sellerId));
        Map<String, Object> returns = returns(sellerId);
        report.put("returnsAndRefunds", returns);

        BigDecimal gross = (BigDecimal) sales.get("grossSales");
        BigDecimal refunded = (BigDecimal) returns.get("totalRefunded");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("grossSales", gross);
        summary.put("totalRefunded", refunded);
        summary.put("netSales", money(gross.subtract(refunded)));
        report.put("summary", summary);
        return report;
    }

    // ------------------------------------------------------------------ products

    private Map<String, Object> products(Seller seller) {
        List<Product> products = new ArrayList<>(productRepositoryPort.findBySeller(seller));
        products.sort(Comparator.comparing(Product::getName).thenComparing(Product::getIdentifier));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", products.size());
        result.put("physical", products.stream().filter(p -> ProductType.PHYSICAL.equals(p.getType())).count());
        result.put("digital", products.stream().filter(p -> ProductType.DIGITAL.equals(p.getType())).count());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productIdentifier", product.getIdentifier());
            row.put("name", product.getName());
            row.put("type", product.getType().getCode());
            row.put("status", product.getStatus().getCode());
            list.add(row);
        }
        result.put("list", list);
        return result;
    }

    // --------------------------------------------------------------------- sales

    private Map<String, Object> sales(String sellerId) {
        Map<String, Long> ordersByStatus = new TreeMap<>();
        Map<String, Map<String, Object>> byProduct = new TreeMap<>();
        int ordersSold = 0;
        int unitsSold = 0;
        BigDecimal gross = BigDecimal.ZERO;

        for (Order order : orderRepositoryPort.findAll()) {
            if (OrderStatus.CART.equals(order.getOrderStatus())) {
                continue;
            }
            List<OrderItem> lines = order.getItems().stream().filter(line -> belongsTo(line, sellerId)).toList();
            if (lines.isEmpty()) {
                continue;
            }
            ordersByStatus.merge(order.getOrderStatus().getCode(), 1L, Long::sum);
            if (!SOLD_STATUSES.contains(order.getOrderStatus())) {
                continue;
            }
            ordersSold++;
            for (OrderItem line : lines) {
                Product product = line.getVariant().getProduct();
                BigDecimal amount = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
                unitsSold += line.getQuantity();
                gross = gross.add(amount);
                Map<String, Object> row = byProduct.computeIfAbsent(product.getName() + "|" + product.getIdentifier(), key -> {
                    Map<String, Object> created = new LinkedHashMap<>();
                    created.put("productIdentifier", product.getIdentifier());
                    created.put("productName", product.getName());
                    created.put("productType", product.getType().getCode());
                    created.put("unitsSold", 0);
                    created.put("amount", BigDecimal.ZERO);
                    return created;
                });
                row.put("unitsSold", (Integer) row.get("unitsSold") + line.getQuantity());
                row.put("amount", money(((BigDecimal) row.get("amount")).add(amount)));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ordersWithSales", ordersSold);
        result.put("ordersByStatus", ordersByStatus);
        result.put("unitsSold", unitsSold);
        result.put("grossSales", money(gross));
        result.put("byProduct", new ArrayList<>(byProduct.values()));
        return result;
    }

    // ----------------------------------------------------------------- inventory

    private Map<String, Object> inventory(String sellerId) {
        List<InventoryItem> items = new ArrayList<>(inventoryItemRepositoryPort.findAll().stream()
                .filter(item -> sellerId.equals(item.getVariant().getProduct().getSeller().getIdentification()))
                .toList());
        items.sort(Comparator.comparing((InventoryItem item) -> item.getVariant().getProduct().getName())
                .thenComparing(item -> item.getVariant().getIdentifier())
                .thenComparing(item -> item.getWarehouse().getIdentifier()));

        Map<String, Integer> movementUnits = new TreeMap<>();
        for (InventoryMovementType type : List.of(InventoryMovementType.INBOUND, InventoryMovementType.RESERVATION,
                InventoryMovementType.SALE_OUTBOUND, InventoryMovementType.ADJUSTMENT, InventoryMovementType.RETURN)) {
            movementUnits.put(type.getCode(), 0);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (InventoryItem item : items) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("inventoryItemIdentifier", item.getIdentifier());
            row.put("productName", item.getVariant().getProduct().getName());
            row.put("variantIdentifier", item.getVariant().getIdentifier());
            row.put("attributes", new TreeMap<>(item.getVariant().getAttributes()));
            row.put("warehouseIdentifier", item.getWarehouse().getIdentifier());
            row.put("warehouseName", item.getWarehouse().getName());
            row.put("quantityAvailable", item.getQuantityAvailable());
            row.put("quantityReserved", item.getQuantityReserved());
            row.put("status", item.getStatus().getCode());
            Map<String, Integer> itemMovements = new TreeMap<>();
            for (InventoryMovement movement : inventoryMovementRepositoryPort.findByInventoryItem(item)) {
                String type = movement.getMovementType().getCode();
                itemMovements.merge(type, movement.getQuantity(), Integer::sum);
                movementUnits.merge(type, movement.getQuantity(), Integer::sum);
            }
            row.put("movementUnitsByType", itemMovements);
            rows.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAvailableUnits", items.stream().mapToInt(InventoryItem::getQuantityAvailable).sum());
        result.put("totalReservedUnits", items.stream().mapToInt(InventoryItem::getQuantityReserved).sum());
        result.put("damagedRecords", items.stream().filter(i -> InventoryItemStatus.DAMAGED.equals(i.getStatus())).count());
        // Changes in stock: INBOUND (+), RESERVATION (purchase confirmed), SALE_OUTBOUND (dispatched),
        // RETURN (+, returned goods), ADJUSTMENT (manual corrections / status changes)
        result.put("movementUnitsByType", movementUnits);
        result.put("items", rows);
        return result;
    }

    // ---------------------------------------------------------- returns/refunds

    private Map<String, Object> returns(String sellerId) {
        List<ReturnRequest> returns = new ArrayList<>(returnRequestRepositoryPort.findAll().stream()
                .filter(r -> r.getVariant() != null
                        && sellerId.equals(r.getVariant().getProduct().getSeller().getIdentification()))
                .toList());
        returns.sort(Comparator.comparing(ReturnRequest::getIdentifier));

        Map<String, Long> returnsByStatus = new TreeMap<>();
        Map<String, Long> refundsByStatus = new TreeMap<>();
        int unitsReturned = 0;
        BigDecimal refunded = BigDecimal.ZERO;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ReturnRequest returnRequest : returns) {
            returnsByStatus.merge(returnRequest.getStatus().getCode(), 1L, Long::sum);
            if (ReturnStatus.RECEIVED.equals(returnRequest.getStatus())
                    || ReturnStatus.COMPLETED.equals(returnRequest.getStatus())) {
                unitsReturned += returnRequest.getQuantity();
            }
            Optional<Refund> refund = refundRepositoryPort.findByReturnRequest(returnRequest);
            refund.ifPresent(r -> refundsByStatus.merge(r.getStatus().getCode(), 1L, Long::sum));
            if (refund.isPresent() && RefundStatus.PROCESSED.equals(refund.get().getStatus())) {
                refunded = refunded.add(refund.get().getAmount());
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("returnIdentifier", returnRequest.getIdentifier());
            row.put("orderIdentifier", returnRequest.getOrder().getIdentifier());
            row.put("productName", returnRequest.getVariant().getProduct().getName());
            row.put("variantIdentifier", returnRequest.getVariant().getIdentifier());
            row.put("quantity", returnRequest.getQuantity());
            row.put("returnStatus", returnRequest.getStatus().getCode());
            row.put("refundStatus", refund.map(r -> r.getStatus().getCode()).orElse(null));
            row.put("refundAmount", refund.map(r -> money(r.getAmount())).orElse(null));
            rows.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalReturns", returns.size());
        result.put("returnsByStatus", returnsByStatus);
        result.put("unitsReturned", unitsReturned);
        result.put("refundsByStatus", refundsByStatus);
        result.put("totalRefunded", money(refunded));
        result.put("items", rows);
        return result;
    }

    private static boolean belongsTo(OrderItem line, String sellerId) {
        return sellerId.equals(line.getVariant().getProduct().getSeller().getIdentification());
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
