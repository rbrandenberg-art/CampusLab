package CampusLab.ms_campuslab_kafka.config;

import CampusLab.ms_campuslab_kafka.security.ConfigurableRolesClaimConverter;
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
 * Mismo patron de seguridad exigido en docs/campuslab-brief.md seccion 4:
 * JWT obligatorio en todos los endpoints salvo /actuator/health, autorizacion
 * por rol usando un claim configurable (roles/scp segun venga de Azure AD).
 *
 * Este servicio es admin-only: el unico endpoint de negocio
 * (GET /api/admin/kafka/topics) exige rol Admin.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${campuslab.security.jwt.roles-claim:roles}")
    private String rolesClaim;

    @Value("${campuslab.security.jwt.scope-claim:scp}")
    private String scopeClaim;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/admin/kafka/**").hasRole("Admin")
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
