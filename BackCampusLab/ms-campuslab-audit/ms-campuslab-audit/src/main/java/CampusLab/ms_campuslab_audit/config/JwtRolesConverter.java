package CampusLab.ms_campuslab_audit.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Extrae authorities desde el claim de roles del token de Azure AD. Todavia no existe un App
 * Registration real, por lo que el nombre exacto del claim ({@code roles} para App Roles o
 * {@code scp} para scopes delegados) se deja configurable en lugar de fijo, tal como pide la
 * seccion 4 del brief.
 */
public class JwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scp";

    private final JwtGrantedAuthoritiesConverter defaultScopesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        String scope = jwt.getClaimAsString(SCOPE_CLAIM);
        if (scope != null && !scope.isBlank()) {
            Arrays.stream(scope.split(" "))
                    .filter(s -> !s.isBlank())
                    .forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
        }

        if (authorities.isEmpty()) {
            // fallback: comportamiento estandar de Spring Security sobre el claim "scope"/"scp".
            Collection<GrantedAuthority> defaults = defaultScopesConverter.convert(jwt);
            if (defaults != null) {
                authorities.addAll(defaults);
            }
        }

        return authorities;
    }

    public static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtRolesConverter());
        return converter;
    }
}
