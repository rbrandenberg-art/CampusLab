package CampusLab.ms_campuslab_catalog.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad JWT resource server según sección 4 del brief: Azure AD aún no
 * tiene App Registration real, por lo que issuer-uri y client-id llegan por
 * variables de entorno con placeholders (CHANGEME-*) para que el servicio
 * compile y levante sin credenciales reales. Todos los endpoints exigen JWT
 * válido salvo /actuator/health. El claim de roles es configurable porque
 * Azure AD puede exponerlo como "roles" o como "scp" según el tipo de token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.roles-claim:roles}")
    private String rolesClaim;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    /**
     * No se define un JwtDecoder manual: Spring Boot autoconfigura uno a partir
     * de `spring.security.oauth2.resourceserver.jwt.issuer-uri` que resuelve el
     * documento OIDC de forma perezosa (en el primer token recibido), no al
     * arrancar. Un decoder manual vía JwtDecoders.fromOidcIssuerLocation(...)
     * haría esa llamada de red al construir el bean, y con el tenant placeholder
     * (CHANGEME-TENANT-ID) el contexto de Spring fallaría al iniciar — justo lo
     * que el brief pide evitar mientras no exista el App Registration real.
     */

    /**
     * Convierte el token a authorities usando el claim de roles configurable
     * (por defecto "roles"; puede ajustarse a "scp" u otro vía
     * app.security.roles-claim cuando exista el App Registration real).
     * <p>
     * Se usa la clase concreta {@link JwtAuthenticationConverter} de Spring
     * Security (no un lambda propio implementando {@code Converter<Jwt,...>}):
     * un lambda pierde la información de tipos genéricos en tiempo de
     * ejecución, y el post-processor de {@code @KafkaListener} escanea todos
     * los beans {@code Converter} del contexto para registrarlos como
     * formatters, lo que revienta el arranque con
     * "Unable to determine source type <S> and target type <T>".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(rolesClaim);
        if (roles == null || roles.isEmpty()) {
            // Azure AD emite scopes delegados en "scp" como string separado por espacios.
            String scope = jwt.getClaimAsString("scp");
            if (scope != null && !scope.isBlank()) {
                roles = List.of(scope.split(" "));
            }
        }
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
