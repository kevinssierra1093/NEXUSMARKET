package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.RequestReturnRequest;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnRequestResponse;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.refund.RequestRefundService;
import application.domain.services.returns.ApproveReturnService;
import application.domain.services.returns.ConfirmReturnReceptionService;
import application.domain.services.returns.ConsultReturnService;
import application.domain.services.returns.RejectReturnService;
import application.domain.services.returns.RequestReturnService;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Transactional
public class ReturnController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RequestReturnService requestReturnService;
    private final ApproveReturnService approveReturnService;
    private final RejectReturnService rejectReturnService;
    private final ConfirmReturnReceptionService confirmReturnReceptionService;
    private final ConsultReturnService consultReturnService;
    private final RequestRefundService requestRefundService;
    private final ConsultRefundService consultRefundService;

    /** BUYER: return of a FINALIZED order. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnRequestResponse request(@Valid @RequestBody RequestReturnRequest request) {
        return CommercialRestMapper.toResponse(requestReturnService.execute(authenticatedUser.resolve(),
                CommercialRestMapper.toReturnRequest(request)));
    }

    @GetMapping("/{identifier}")
    public ReturnRequestResponse consult(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(consultReturnService.executeById(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }

    /** ADMINISTRATOR / SUPERVISOR, e.g. /api/returns?status=REQUESTED. */
    @GetMapping
    public List<ReturnRequestResponse> consultByStatus(@RequestParam String status) {
        return consultReturnService.executeByStatus(authenticatedUser.resolve(), RestReferences.returnStatusFilter(status))
                .stream().map(CommercialRestMapper::toResponse).toList();
    }

    /** ADMINISTRATOR. */
    @PostMapping("/{identifier}/approve")
    public ReturnRequestResponse approve(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(approveReturnService.execute(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }

    /** ADMINISTRATOR. */
    @PostMapping("/{identifier}/reject")
    public ReturnRequestResponse reject(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(rejectReturnService.execute(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }

    /** LOGISTICS_OPERATOR: the returned units re-enter the origin warehouses. */
    @PostMapping("/{identifier}/reception")
    public ReturnRequestResponse confirmReception(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(confirmReturnReceptionService.execute(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }

    /** BUYER (owner) or ADMINISTRATOR: requests the refund of the whole order. */
    @PostMapping("/{identifier}/refund")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse requestRefund(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(requestRefundService.execute(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }

    @GetMapping("/{identifier}/refund")
    public RefundResponse consultRefund(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(consultRefundService.executeByReturnRequest(authenticatedUser.resolve(),
                RestReferences.returnRequest(identifier)));
    }
}
