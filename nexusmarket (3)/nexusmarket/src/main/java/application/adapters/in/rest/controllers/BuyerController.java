package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.ParticipantRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ReturnRequestResponse;
import application.domain.services.buyer.ChangeBuyerStatusService;
import application.domain.services.buyer.ConsultBuyerService;
import application.domain.services.buyer.UpdateBuyerService;
import application.domain.services.order.CalculateOrderTotalService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.valueobjects.BuyerStatus;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.fromCode;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Transactional
public class BuyerController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConsultBuyerService consultBuyerService;
    private final UpdateBuyerService updateBuyerService;
    private final ChangeBuyerStatusService changeBuyerStatusService;
    private final ConsultOrderService consultOrderService;
    private final ConsultReturnService consultReturnService;
    private final CalculateOrderTotalService calculateOrderTotalService;

    @GetMapping
    public List<BuyerResponse> consultAll() {
        return consultBuyerService.executeAll(authenticatedUser.resolve()).stream()
                .map(ParticipantRestMapper::toResponse).toList();
    }

    @GetMapping("/{identification}")
    public BuyerResponse consult(@PathVariable String identification) {
        return ParticipantRestMapper.toResponse(
                consultBuyerService.execute(authenticatedUser.resolve(), RestReferences.buyer(identification)));
    }

    @PutMapping("/{identification}")
    public BuyerResponse update(@PathVariable String identification, @Valid @RequestBody UpdateBuyerRequest request) {
        return ParticipantRestMapper.toResponse(updateBuyerService.execute(authenticatedUser.resolve(),
                ParticipantRestMapper.toBuyer(identification, request)));
    }

    @PatchMapping("/{identification}/status")
    public BuyerResponse changeStatus(@PathVariable String identification,
                                      @Valid @RequestBody ChangeStatusRequest request) {
        return ParticipantRestMapper.toResponse(changeBuyerStatusService.execute(authenticatedUser.resolve(),
                RestReferences.buyer(identification), fromCode(BuyerStatus.class, request.status())));
    }

    @GetMapping("/{identification}/orders")
    public List<OrderResponse> orders(@PathVariable String identification) {
        return consultOrderService.executeByBuyer(authenticatedUser.resolve(), RestReferences.buyer(identification))
                .stream().map(order -> CommercialRestMapper.toResponse(order, calculateOrderTotalService.execute(order)))
                .toList();
    }

    @GetMapping("/{identification}/returns")
    public List<ReturnRequestResponse> returns(@PathVariable String identification) {
        return consultReturnService.executeByBuyer(authenticatedUser.resolve(), RestReferences.buyer(identification))
                .stream().map(CommercialRestMapper::toResponse).toList();
    }
}
