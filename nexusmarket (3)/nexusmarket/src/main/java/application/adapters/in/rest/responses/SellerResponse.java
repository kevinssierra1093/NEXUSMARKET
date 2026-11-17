package application.adapters.in.rest.responses;

import java.time.LocalDate;
import java.util.List;

/** warehouses and products are only populated when consulting a single seller. */
public record SellerResponse(
        String identification,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String businessName,
        LocalDate registrationDate,
        String status,
        List<WarehouseResponse> warehouses,
        List<ProductResponse> products) {
}
