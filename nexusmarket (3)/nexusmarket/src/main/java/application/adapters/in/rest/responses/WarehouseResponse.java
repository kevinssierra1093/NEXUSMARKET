package application.adapters.in.rest.responses;

public record WarehouseResponse(
        String identifier,
        String name,
        String address,
        String type,
        String ownerIdentification,
        String ownerBusinessName,
        String status) {
}
