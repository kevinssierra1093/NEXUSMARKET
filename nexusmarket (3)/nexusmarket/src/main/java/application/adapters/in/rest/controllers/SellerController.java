package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CatalogRestMapper;
import application.adapters.in.rest.mappers.ParticipantRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.RegisterSellerRequest;
import application.adapters.in.rest.requests.UpdateSellerRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.SellerResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.services.catalog.ConsultPublishedCatalogService;
import application.domain.services.seller.ChangeSellerStatusService;
import application.domain.services.seller.ConsultSellerService;
import application.domain.services.seller.RegisterSellerService;
import application.domain.services.seller.UpdateSellerService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.valueobjects.SellerStatus;
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
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Transactional
public class SellerController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RegisterSellerService registerSellerService;
    private final ConsultSellerService consultSellerService;
    private final UpdateSellerService updateSellerService;
    private final ChangeSellerStatusService changeSellerStatusService;
    private final ConsultWarehouseService consultWarehouseService;
    private final ConsultPublishedCatalogService consultPublishedCatalogService;

    /** ADMINISTRATOR: incorporates the seller and its first warehouse (one transaction). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SellerResponse register(@Valid @RequestBody RegisterSellerRequest request) {
        return ParticipantRestMapper.toResponse(registerSellerService.execute(authenticatedUser.resolve(),
                ParticipantRestMapper.toSeller(request), ParticipantRestMapper.toWarehouse(request.firstWarehouse())));
    }

    @GetMapping
    public List<SellerResponse> consultAll() {
        return consultSellerService.executeAll(authenticatedUser.resolve()).stream()
                .map(ParticipantRestMapper::toResponse).toList();
    }

    @GetMapping("/{identification}")
    public SellerResponse consult(@PathVariable String identification) {
        return ParticipantRestMapper.toResponse(
                consultSellerService.execute(authenticatedUser.resolve(), RestReferences.seller(identification)));
    }

    @PutMapping("/{identification}")
    public SellerResponse update(@PathVariable String identification, @Valid @RequestBody UpdateSellerRequest request) {
        return ParticipantRestMapper.toResponse(updateSellerService.execute(authenticatedUser.resolve(),
                ParticipantRestMapper.toSeller(identification, request)));
    }

    @PatchMapping("/{identification}/status")
    public SellerResponse changeStatus(@PathVariable String identification,
                                       @Valid @RequestBody ChangeStatusRequest request) {
        return ParticipantRestMapper.toResponse(changeSellerStatusService.execute(authenticatedUser.resolve(),
                RestReferences.seller(identification), fromCode(SellerStatus.class, request.status())));
    }

    @GetMapping("/{identification}/warehouses")
    public List<WarehouseResponse> warehouses(@PathVariable String identification) {
        return consultWarehouseService.executeBySeller(authenticatedUser.resolve(), RestReferences.seller(identification))
                .stream().map(CatalogRestMapper::toResponse).toList();
    }

    @GetMapping("/{identification}/products")
    public List<ProductResponse> products(@PathVariable String identification) {
        return consultPublishedCatalogService.executeBySeller(authenticatedUser.resolve(),
                        RestReferences.seller(identification))
                .stream().map(CatalogRestMapper::toResponse).toList();
    }
}
