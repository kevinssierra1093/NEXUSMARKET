package application.infrastructure.security;

/** Identity extracted from a valid JWT and stored in the SecurityContext. */
public record AuthenticatedPrincipal(Integer userId, String username, String role) {
}
