package application.domain.services.report;

import application.domain.models.Refund;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultReturnsReportService {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final RefundRepositoryPort refundRepositoryPort;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public Map<String, Object> execute(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        List<ReturnRequest> returns = returnRequestRepositoryPort.findAll();
        List<Refund> refunds = refundRepositoryPort.findAll();
        Map<String, Long> returnsByStatus = returns.stream().collect(Collectors.groupingBy(
                request -> request.getStatus().getCode(), TreeMap::new, Collectors.counting()));
        Map<String, Long> refundsByStatus = refunds.stream().collect(Collectors.groupingBy(
                refund -> refund.getStatus().getCode(), TreeMap::new, Collectors.counting()));
        BigDecimal totalRefunded = refunds.stream()
                .filter(refund -> RefundStatus.PROCESSED.equals(refund.getStatus()))
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalReturns", returns.size());
        report.put("returnsByStatus", returnsByStatus);
        report.put("refundsByStatus", refundsByStatus);
        report.put("totalRefunded", totalRefunded);
        return report;
    }
}
