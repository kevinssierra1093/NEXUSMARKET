package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Gives the controllers the authenticated domain User ("requestingUser"), loaded fresh from the
 * database together with its participant (Buyer/Seller). The current status is therefore always
 * re-checked by the domain: a user blocked after receiving a token is rejected.
 * Returns null for anonymous requests; the domain then raises UnauthorizedOperationException (RG-01).
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final UserRepositoryPort userRepositoryPort;

    public User resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            return null;
        }
        User criteria = new User();
        criteria.setUserId(principal.userId());
        return userRepositoryPort.findById(criteria).orElse(null);
    }
}
