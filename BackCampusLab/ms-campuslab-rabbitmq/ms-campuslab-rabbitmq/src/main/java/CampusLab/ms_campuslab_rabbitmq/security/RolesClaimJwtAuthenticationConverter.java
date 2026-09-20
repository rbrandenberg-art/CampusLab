package CampusLab.ms_campuslab_rabbitmq.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte el JWT de Azure AD en authorities de Spring Security.
 * <p>
 * Aún no existe App Registration real en Azure AD, por lo que el nombre
 * exacto del claim de roles puede venir como {@code roles} (App roles) o
 * {@code scp} (scopes delegados) según cómo quede configurado el tenant
 * real. Este converter revisa primero el claim configurable
 * ({@code app.security.roles-claim}, por defecto {@code roles}) y, si no
 * viene, cae a {@code scp} (separado por espacios, como es habitual en
 * tokens de Azure AD).
 */
@Component
public class RolesClaimJwtAuthenticationConverter implements org.springframework.core.convert.converter.Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String FALLBACK_CLAIM = "scp";

    private final String rolesClaimName;

    public RolesClaimJwtAuthenticationConverter(@Value("${app.security.roles-claim:roles}") String rolesClaimName) {
        this.rolesClaimName = rolesClaimName;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> rawRoles = readClaimAsList(jwt, rolesClaimName);
        if (rawRoles.isEmpty()) {
            rawRoles = readClaimAsList(jwt, FALLBACK_CLAIM);
        }
        return rawRoles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<String> readClaimAsList(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).collect(Collectors.toList());
        }
        if (claim instanceof String claimString && !claimString.isBlank()) {
            return List.of(claimString.split("\\s+"));
        }
        return List.of();
    }
}
