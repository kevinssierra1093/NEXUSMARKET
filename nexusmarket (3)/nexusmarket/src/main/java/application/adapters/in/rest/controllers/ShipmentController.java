package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.domain.services.shipment.ConfirmShipmentDeliveryService;
import application.domain.services.shipment.UpdateShipmentStatusService;
import application.domain.valueobjects.ShipmentStatus;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static application.adapters.shared.ValueObjectResolver.fromCode;

/** LOGISTICS_OPERATOR. Shipments are listed through /api/orders/{id}/shipments. */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Transactional
public class ShipmentController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final UpdateShipmentStatusService updateShipmentStatusService;
    private final ConfirmShipmentDeliveryService confirmShipmentDeliveryService;

    /** status: IN_TRANSIT or FAILED (dispatch and delivery have their own endpoints). */
    @PatchMapping("/{identifier}/status")
    public ShipmentResponse changeStatus(@PathVariable String identifier,
                                         @Valid @RequestBody ChangeStatusRequest request) {
        return CommercialRestMapper.toResponse(updateShipmentStatusService.execute(authenticatedUser.resolve(),
                RestReferences.shipment(identifier), fromCode(ShipmentStatus.class, request.status())));
    }

    /** Confirms the delivery; when every shipment of the order is delivered the order is finalized. */
    @PostMapping("/{identifier}/delivery")
    public ShipmentResponse confirmDelivery(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(confirmShipmentDeliveryService.execute(authenticatedUser.resolve(),
                RestReferences.shipment(identifier)));
    }
}
