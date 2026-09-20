package CampusLab.ms_campuslab_bookings.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Extrae authorities desde un JWT sin asumir el nombre exacto del claim de
 * roles: Azure AD puede entregarlos como "roles" (App Roles, colección) o
 * como "scp" (scopes delegados, string separado por espacios), según cómo
 * quede configurado el App Registration real (aún pendiente, sección 4 del
 * brief). Ambos nombres de claim son configurables por propiedades para no
 * tener que tocar código cuando exista el App Registration definitivo.
 */
public class ConfigurableRolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String rolesClaim;
    private final String scopeClaim;

    public ConfigurableRolesClaimConverter(String rolesClaim, String scopeClaim) {
        this.rolesClaim = rolesClaim;
        this.scopeClaim = scopeClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>();
        collectRoleNames(jwt.getClaims().get(rolesClaim))
                .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        collectRoleNames(jwt.getClaims().get(scopeClaim))
                .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        return authorities;
    }

    private List<String> collectRoleNames(Object claimValue) {
        if (claimValue instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        if (claimValue instanceof String text && !text.isBlank()) {
            return Arrays.stream(text.trim().split("\\s+")).toList();
        }
        return List.of();
    }
}
