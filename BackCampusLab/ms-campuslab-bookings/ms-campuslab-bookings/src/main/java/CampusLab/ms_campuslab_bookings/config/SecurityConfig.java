package CampusLab.ms_campuslab_bookings.config;

import CampusLab.ms_campuslab_bookings.security.ConfigurableRolesClaimConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad JWT resource server según sección 4 del brief: Azure AD aún no
 * tiene App Registration real, por lo que issuer-uri, tenant-id y client-id
 * llegan por variables de entorno con placeholders (CHANGEME-*). El
 * JwtDecoder se arma de forma perezosa a partir de solo la propiedad
 * issuer-uri (autoconfiguración de Spring Boot, sin bean propio) para que el
 * servicio compile y levante igual sin credenciales ni tenant reales: la
 * resolución OIDC solo ocurre al validar el primer token, no al iniciar.
 * Todos los endpoints exigen JWT válido salvo /actuator/health. El claim de
 * roles es configurable porque Azure AD puede exponerlo como "roles" o como
 * "scp" según el tipo de token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.roles-claim:roles}")
    private String rolesClaim;

    @Value("${app.security.scope-claim:scp}")
    private String scopeClaim;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new ConfigurableRolesClaimConverter(rolesClaim, scopeClaim));
        return converter;
    }
}
