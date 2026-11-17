package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.AuditRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.responses.AuditLogResponse;
import application.adapters.in.rest.responses.OperationResponse;
import application.domain.exceptions.DomainException;
import application.domain.models.AuditLog;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.services.operation.ConsultAuditLogsService;
import application.domain.services.operation.ConsultOperationsService;
import application.infrastructure.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADMINISTRATOR / SUPERVISOR. Exactly one filter must be given:
 * ?userId=7  |  ?type=ORDER_PAYMENT  |  ?entityType=Order&entityId=...
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final ConsultOperationsService consultOperationsService;
    private final ConsultAuditLogsService consultAuditLogsService;

    @GetMapping("/operations")
    public List<OperationResponse> operations(@RequestParam(required = false) Integer userId,
                                              @RequestParam(required = false) String type,
                                              @RequestParam(required = false) String entityType,
                                              @RequestParam(required = false) String entityId) {
        User requestingUser = authenticatedUser.resolve();
        List<Operation> operations = switch (selectFilter(userId, type, entityType, entityId)) {
            case USER -> consultOperationsService.executeByUser(requestingUser, RestReferences.user(userId));
            case TYPE -> consultOperationsService.executeByType(requestingUser, RestReferences.operationTypeFilter(type));
            case ENTITY -> consultOperationsService.executeByEntity(requestingUser,
                    RestReferences.traceableEntity(entityType, entityId));
        };
        return operations.stream().map(AuditRestMapper::toResponse).toList();
    }

    @GetMapping("/logs")
    public List<AuditLogResponse> logs(@RequestParam(required = false) Integer userId,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) String entityType,
                                       @RequestParam(required = false) String entityId) {
        User requestingUser = authenticatedUser.resolve();
        List<AuditLog> logs = switch (selectFilter(userId, type, entityType, entityId)) {
            case USER -> consultAuditLogsService.executeByUser(requestingUser, RestReferences.user(userId));
            case TYPE -> consultAuditLogsService.executeByOperationType(requestingUser,
                    RestReferences.auditTypeFilter(type));
            case ENTITY -> consultAuditLogsService.executeByEntity(requestingUser,
                    RestReferences.traceableEntity(entityType, entityId));
        };
        return logs.stream().map(AuditRestMapper::toResponse).toList();
    }

    private enum Filter { USER, TYPE, ENTITY }

    private Filter selectFilter(Integer userId, String type, String entityType, String entityId) {
        boolean byUser = userId != null;
        boolean byType = type != null && !type.isBlank();
        boolean byEntity = entityType != null && !entityType.isBlank() && entityId != null && !entityId.isBlank();
        int count = (byUser ? 1 : 0) + (byType ? 1 : 0) + (byEntity ? 1 : 0);
        if (count != 1) {
            throw new DomainException("Use exactly one filter: userId, type, or entityType + entityId.");
        }
        return byUser ? Filter.USER : byType ? Filter.TYPE : Filter.ENTITY;
    }
}
