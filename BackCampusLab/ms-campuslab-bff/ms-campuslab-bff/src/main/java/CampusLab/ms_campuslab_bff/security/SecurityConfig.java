package CampusLab.ms_campuslab_bff.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                        //.requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "TECNICO", "ESTUDIANTE")
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Reportes: solo lectura, solo Admin.
                        .requestMatchers(HttpMethod.GET, "/api/report/**").hasRole("ADMIN")

                        // Auditoria: solo lectura, Admin y Auditor.
                        .requestMatchers(HttpMethod.GET, "/api/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${campuslab.security.jwt.roles-claim:roles}") String rolesClaim) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RolesClaimGrantedAuthoritiesConverter(rolesClaim));
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}