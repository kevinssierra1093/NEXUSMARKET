package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CatalogRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.InventoryAdjustmentRequest;
import application.adapters.in.rest.requests.InventoryInboundRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.domain.services.inventory.AdjustInventoryService;
import application.domain.services.inventory.ChangeInventoryItemStatusService;
import application.domain.services.inventory.ConsultInventoryMovementsService;
import application.domain.services.inventory.RegisterInventoryInboundService;
import application.domain.valueobjects.InventoryItemStatus;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.fromCode;

/** Owning SELLER or LOGISTICS_OPERATOR (validated by the domain). */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Transactional
public class InventoryController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RegisterInventoryInboundService registerInventoryInboundService;
    private final AdjustInventoryService adjustInventoryService;
    private final ChangeInventoryItemStatusService changeInventoryItemStatusService;
    private final ConsultInventoryMovementsService consultInventoryMovementsService;

    @PostMapping("/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse inbound(@Valid @RequestBody InventoryInboundRequest request) {
        return CatalogRestMapper.toResponse(registerInventoryInboundService.execute(authenticatedUser.resolve(),
                CatalogRestMapper.toInventoryItem(request), request.quantity()));
    }

    @PatchMapping("/{identifier}/adjustment")
    public InventoryItemResponse adjust(@PathVariable String identifier,
                                        @Valid @RequestBody InventoryAdjustmentRequest request) {
        return CatalogRestMapper.toResponse(adjustInventoryService.execute(authenticatedUser.resolve(),
                RestReferences.inventoryItem(identifier), request.newQuantityAvailable()));
    }

    /** status: AVAILABLE or DAMAGED. */
    @PatchMapping("/{identifier}/status")
    public InventoryItemResponse changeStatus(@PathVariable String identifier,
                                              @Valid @RequestBody ChangeStatusRequest request) {
        return CatalogRestMapper.toResponse(changeInventoryItemStatusService.execute(authenticatedUser.resolve(),
                RestReferences.inventoryItem(identifier), fromCode(InventoryItemStatus.class, request.status())));
    }

    @GetMapping("/{identifier}/movements")
    public List<InventoryMovementResponse> movements(@PathVariable String identifier) {
        return consultInventoryMovementsService.execute(authenticatedUser.resolve(),
                        RestReferences.inventoryItem(identifier))
                .stream().map(CatalogRestMapper::toResponse).toList();
    }
}
