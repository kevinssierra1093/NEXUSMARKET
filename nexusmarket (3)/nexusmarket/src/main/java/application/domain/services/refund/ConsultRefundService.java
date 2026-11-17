package application.domain.services.refund;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.AuthorizeRefundOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultRefundService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final AuthorizeRefundOperationService authorizeRefundOperationService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public Refund executeByReturnRequest(User requestingUser, ReturnRequest returnRequest) {
        ReturnRequest stored = returnRequestRepositoryPort.findByIdentifier(returnRequest)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest"));
        authorizeRefundOperationService.executeConsultation(requestingUser, stored.getBuyer());
        return refundRepositoryPort.findByReturnRequest(stored)
                .orElseThrow(() -> new EntityNotFoundException("Refund"));
    }

    public List<Refund> executeAll(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return refundRepositoryPort.findAll();
    }
}
