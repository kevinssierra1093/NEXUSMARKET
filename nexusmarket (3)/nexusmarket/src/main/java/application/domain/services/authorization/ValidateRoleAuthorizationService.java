package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/** RG-02 / RG-03: a user has a single role and may only act within it. */
@Service
@RequiredArgsConstructor
public class ValidateRoleAuthorizationService {

    public void execute(User user, SystemRole... allowedRoles) {
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(role -> role.equals(user.getRole()));
        if (!allowed) {
            String required = String.join(", ", Arrays.stream(allowedRoles).map(SystemRole::getCode).toList());
            throw new UnauthorizedOperationException(
                    "One of the roles [" + required + "] is required for this operation.");
        }
    }
}
