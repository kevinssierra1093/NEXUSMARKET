package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.CommercialRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.responses.RefundResponse;
import application.domain.services.refund.ApproveRefundService;
import application.domain.services.refund.ConsultRefundService;
import application.domain.services.refund.ProcessRefundService;
import application.domain.services.refund.RejectRefundService;
import application.infrastructure.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Refund resolution: ADMINISTRATOR. Refunds are requested through /api/returns/{id}/refund. */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Transactional
public class RefundController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConsultRefundService consultRefundService;
    private final ApproveRefundService approveRefundService;
    private final RejectRefundService rejectRefundService;
    private final ProcessRefundService processRefundService;

    @GetMapping
    public List<RefundResponse> consultAll() {
        return consultRefundService.executeAll(authenticatedUser.resolve()).stream()
                .map(CommercialRestMapper::toResponse).toList();
    }

    @PostMapping("/{identifier}/approve")
    public RefundResponse approve(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(
                approveRefundService.execute(authenticatedUser.resolve(), RestReferences.refund(identifier)));
    }

    @PostMapping("/{identifier}/reject")
    public RefundResponse reject(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(
                rejectRefundService.execute(authenticatedUser.resolve(), RestReferences.refund(identifier)));
    }

    /** APPROVED -> PROCESSED: completes the return and annuls the invoice (one transaction). */
    @PostMapping("/{identifier}/process")
    public RefundResponse process(@PathVariable String identifier) {
        return CommercialRestMapper.toResponse(
                processRefundService.execute(authenticatedUser.resolve(), RestReferences.refund(identifier)));
    }
}
