package CampusLab.ms_campuslab_rabbitmq.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security como resource server JWT (sección 4 del brief CampusLab).
 * <p>
 * Todos los endpoints requieren JWT válido salvo {@code /actuator/health}.
 * El endpoint de topología además exige el rol Admin vía
 * {@code @PreAuthorize} en {@link CampusLab.ms_campuslab_rabbitmq.web.RabbitTopologyController}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RolesClaimJwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(RolesClaimJwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }
}
