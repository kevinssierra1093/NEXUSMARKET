package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CatalogRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.requests.WarehouseDataRequest;
import application.adapters.in.rest.responses.InventoryItemResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.warehouse.ChangeWarehouseStatusService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.RegisterWarehouseService;
import application.domain.services.warehouse.UpdateWarehouseService;
import application.domain.valueobjects.WarehouseStatus;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.fromCode;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Transactional
public class WarehouseController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RegisterWarehouseService registerWarehouseService;
    private final ConsultWarehouseService consultWarehouseService;
    private final UpdateWarehouseService updateWarehouseService;
    private final ChangeWarehouseStatusService changeWarehouseStatusService;
    private final ConsultInventoryService consultInventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse register(@Valid @RequestBody RegisterWarehouseRequest request) {
        return CatalogRestMapper.toResponse(
                registerWarehouseService.execute(authenticatedUser.resolve(), CatalogRestMapper.toWarehouse(request)));
    }

    @GetMapping
    public List<WarehouseResponse> consultAll() {
        return consultWarehouseService.executeAll(authenticatedUser.resolve()).stream()
                .map(CatalogRestMapper::toResponse).toList();
    }

    @GetMapping("/{identifier}")
    public WarehouseResponse consult(@PathVariable String identifier) {
        return CatalogRestMapper.toResponse(
                consultWarehouseService.execute(authenticatedUser.resolve(), RestReferences.warehouse(identifier)));
    }

    @PutMapping("/{identifier}")
    public WarehouseResponse update(@PathVariable String identifier, @Valid @RequestBody WarehouseDataRequest request) {
        return CatalogRestMapper.toResponse(updateWarehouseService.execute(authenticatedUser.resolve(),
                CatalogRestMapper.toWarehouse(identifier, request)));
    }

    @PatchMapping("/{identifier}/status")
    public WarehouseResponse changeStatus(@PathVariable String identifier,
                                          @Valid @RequestBody ChangeStatusRequest request) {
        return CatalogRestMapper.toResponse(changeWarehouseStatusService.execute(authenticatedUser.resolve(),
                RestReferences.warehouse(identifier), fromCode(WarehouseStatus.class, request.status())));
    }

    @GetMapping("/{identifier}/inventory")
    public List<InventoryItemResponse> inventory(@PathVariable String identifier) {
        return consultInventoryService.executeByWarehouse(authenticatedUser.resolve(),
                        RestReferences.warehouse(identifier))
                .stream().map(CatalogRestMapper::toResponse).toList();
    }
}
