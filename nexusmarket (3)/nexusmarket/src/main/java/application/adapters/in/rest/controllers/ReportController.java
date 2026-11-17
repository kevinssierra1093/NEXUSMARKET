package application.adapters.in.rest.controllers;

import application.domain.services.report.ConsultInventoryReportService;
import application.domain.services.report.ConsultOrdersReportService;
import application.domain.services.report.ConsultReturnsReportService;
import application.domain.services.report.ConsultSellerReportService;
import application.adapters.in.rest.mappers.RestReferences;
import application.infrastructure.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Global reports: ADMINISTRATOR / SUPERVISOR. Seller reports: the owning SELLER, or
 * ADMINISTRATOR / SUPERVISOR. The reports are returned as produced by the domain: the keys are
 * documented in SDD/domain/services/report-services.md.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConsultOrdersReportService consultOrdersReportService;
    private final ConsultInventoryReportService consultInventoryReportService;
    private final ConsultReturnsReportService consultReturnsReportService;
    private final ConsultSellerReportService consultSellerReportService;

    @GetMapping("/orders")
    public Map<String, Object> orders() {
        return consultOrdersReportService.execute(authenticatedUser.resolve());
    }

    @GetMapping("/inventory")
    public Map<String, Object> inventory() {
        return consultInventoryReportService.execute(authenticatedUser.resolve());
    }

    @GetMapping("/returns")
    public Map<String, Object> returns() {
        return consultReturnsReportService.execute(authenticatedUser.resolve());
    }

    /** Report of every seller (ADMINISTRATOR / SUPERVISOR). */
    @GetMapping("/sellers")
    public List<Map<String, Object>> sellers() {
        return consultSellerReportService.executeAll(authenticatedUser.resolve());
    }

    /** Report of one seller: the seller itself, or ADMINISTRATOR / SUPERVISOR. */
    @GetMapping("/sellers/{identification}")
    public Map<String, Object> seller(@PathVariable String identification) {
        return consultSellerReportService.execute(authenticatedUser.resolve(), RestReferences.seller(identification));
    }
}
