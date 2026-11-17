package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

public record ShipmentResponse(
        String identifier,
        String orderIdentifier,
        String originWarehouseIdentifier,
        String originWarehouseName,
        String handledBy,
        String status,
        LocalDateTime dispatchDate,
        LocalDateTime deliveryDate) {
}
