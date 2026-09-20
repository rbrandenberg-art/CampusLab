package CampusLab.ms_campuslab_report.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Seguridad segun seccion 4 del brief de CampusLab: JWT (resource server) obligatorio en
 * todos los endpoints salvo /actuator/health. Azure AD aun no tiene App Registration real,
 * por eso el issuer-uri usa un tenant placeholder configurable por variable de entorno
 * (ver application.yaml) y el claim de roles es configurable, ya que Azure AD puede
 * exponerlo como "roles" o como "scp" segun el tipo de token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${campuslab.security.roles-claim:roles}")
    private String rolesClaimName;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Convierte el JWT en authorities de Spring Security leyendo el claim configurado
     * (por defecto "roles"); si no viene presente, intenta con "scp" (formato de scopes
     * de Azure AD para tokens delegados). Cada valor se antepone con "ROLE_".
     */
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> values = readClaimAsList(jwt, rolesClaimName);
        if (values.isEmpty() && !"scp".equalsIgnoreCase(rolesClaimName)) {
            values = readClaimAsList(jwt, "scp");
        }
        return values.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<String> readClaimAsList(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim == null) {
            return Collections.emptyList();
        }
        if (claim instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).collect(Collectors.toList());
        }
        // Azure AD puede enviar "scp" como un string con scopes separados por espacio
        String text = String.valueOf(claim);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(text.trim().split("\\s+"));
    }
}
