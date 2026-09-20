package CampusLab.ms_campuslab_notify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ms-campuslab-notify es un consumidor RabbitMQ puro: no expone API pública
 * (sección 5 del brief). El único endpoint HTTP presente es
 * {@code /actuator/health} (requerido por la sección 4), por lo que no hay
 * recurso de negocio que proteger con JWT.
 * <p>
 * Por eso, a diferencia de los servicios con endpoints de negocio, aquí NO se
 * configura el resource server OAuth2/JWT contra Azure AD: bastaría con
 * bloquear el health check sin motivo. Si en el futuro este servicio expone
 * endpoints propios, debe migrarse al mismo patrón JWT (issuer-uri +
 * converter de roles) usado en el resto de microservicios.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .build();
    }
}
