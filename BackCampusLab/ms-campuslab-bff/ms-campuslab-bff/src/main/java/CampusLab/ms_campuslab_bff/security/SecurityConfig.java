package CampusLab.ms_campuslab_bff.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Fachada de seguridad del BFF: exige JWT valido en todo endpoint salvo {@code /actuator/health}
 * y autoriza por rol ANTES de reenviar al microservicio de dominio correspondiente (seccion 9
 * del brief, tabla de pantallas/roles).
 *
 * <p>Reglas de autorizacion:</p>
 * <ul>
 *   <li>{@code /api/catalog/**} escritura (POST/PUT/PATCH/DELETE) -> solo ADMIN.</li>
 *   <li>{@code /api/catalog/**} lectura (GET) -> ADMIN, TECNICO.</li>
 *   <li>{@code /api/bookings/**} cualquier metodo -> ADMIN, TECNICO, ESTUDIANTE.</li>
 *   <li>{@code /api/report/**} (solo GET) -> ADMIN.</li>
 *   <li>{@code /api/audit/**} (solo GET) -> ADMIN, AUDITOR.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()

                        // Catalogo: escritura solo Admin, lectura Admin + Tecnico.
                        .requestMatchers(HttpMethod.POST, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").hasAnyRole("ADMIN", "TECNICO")

                        // Reservas: Admin, Tecnico y Estudiante para cualquier metodo.
                        .requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "TECNICO", "ESTUDIANTE")

                        // Reportes: solo lectura, solo Admin.
                        .requestMatchers(HttpMethod.GET, "/api/report/**").hasRole("ADMIN")

                        // Auditoria: solo lectura, Admin y Auditor.
                        .requestMatchers(HttpMethod.GET, "/api/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Convertidor de autenticacion JWT -> authorities. El nombre del claim de roles es
     * configurable (placeholder hasta que exista el App Registration real de Azure AD).
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${campuslab.security.jwt.roles-claim:roles}") String rolesClaim) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RolesClaimGrantedAuthoritiesConverter(rolesClaim));
        return converter;
    }
}
