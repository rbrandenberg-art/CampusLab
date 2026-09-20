package CampusLab.ms_campuslab_bff.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extrae los roles del JWT y los convierte en {@link GrantedAuthority} con prefijo {@code ROLE_}.
 *
 * <p>Aun no existe el App Registration real de Azure AD (ver seccion 4 del brief), por lo que el
 * nombre del claim de roles es configurable via {@code campuslab.security.jwt.roles-claim}
 * (env {@code JWT_ROLES_CLAIM}, default {@code roles}). Si el claim configurado no viene en el
 * token, se intenta el claim {@code scp} (scopes delegados) como respaldo.</p>
 *
 * <p>Los valores esperados por rol de negocio (ver seccion 2 del brief) son, sin acentos:
 * {@code ADMIN}, {@code TECNICO}, {@code ESTUDIANTE}, {@code AUDITOR}.</p>
 */
public class RolesClaimGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String FALLBACK_CLAIM = "scp";

    private final String rolesClaim;

    public RolesClaimGrantedAuthoritiesConverter(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return extractRoles(jwt).stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase(Locale.ROOT)))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private List<String> extractRoles(Jwt jwt) {
        List<String> roles = readClaim(jwt, rolesClaim);
        if (!roles.isEmpty()) {
            return roles;
        }
        if (!FALLBACK_CLAIM.equals(rolesClaim)) {
            return readClaim(jwt, FALLBACK_CLAIM);
        }
        return List.of();
    }

    private List<String> readClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        if (claim instanceof String value && !value.isBlank()) {
            return Arrays.asList(value.trim().split("[\\s,]+"));
        }
        return List.of();
    }

    /** Util de test/documentacion: roles de negocio soportados (seccion 2 del brief). */
    public static Set<String> knownBusinessRoles() {
        return Set.of("ADMIN", "TECNICO", "ESTUDIANTE", "AUDITOR");
    }
}
