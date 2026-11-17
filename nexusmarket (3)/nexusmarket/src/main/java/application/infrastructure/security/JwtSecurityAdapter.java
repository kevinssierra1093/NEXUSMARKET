package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Implements JwtServicePort: issues a signed token with the user id, username and role. */
@Component
public class JwtSecurityAdapter implements JwtServicePort {

    public static final String USER_ID_CLAIM = "userId";
    public static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;
    private final String issuer;

    public JwtSecurityAdapter(JwtEncoder jwtEncoder,
                              @Value("${security.jwt.expiration-minutes}") long expirationMinutes,
                              @Value("${security.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getUserId())
                .claim(ROLE_CLAIM, user.getRole().getCode())
                .build();
        // "typ": "JWT" is set explicitly: Spring Security 7 validates the typ header when decoding.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
