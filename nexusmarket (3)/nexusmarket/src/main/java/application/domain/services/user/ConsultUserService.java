package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeAdministrativeConsultationService;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConsultUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final AuthorizeAdministrativeConsultationService authorizeAdministrativeConsultationService;

    public User execute(User requestingUser, User user) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        User stored = userRepositoryPort.findById(user)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        boolean self = Objects.equals(requestingUser.getUserId(), stored.getUserId());
        if (!self) {
            try {
                authorizeAdministrativeConsultationService.execute(requestingUser);
            } catch (UnauthorizedOperationException e) {
                throw new UnauthorizedOperationException("A user can only consult their own information.");
            }
        }
        return stored;
    }

    public List<User> executeAll(User requestingUser) {
        authorizeAdministrativeConsultationService.execute(requestingUser);
        return userRepositoryPort.findAll();
    }
}
