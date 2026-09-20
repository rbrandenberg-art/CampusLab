package CampusLab.ms_campuslab_bookings.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ms-campuslab-bookings solo publica comandos (no declara colas/exchanges:
 * esa topología, sección 7 del brief, es responsabilidad de
 * ms-campuslab-rabbitmq). Aquí solo se configura la serialización JSON del
 * envelope común, reutilizando el ObjectMapper autoconfigurado por Spring
 * Boot (ya trae el módulo JSR-310 para timestamps ISO-8601). Spring Boot
 * conecta automáticamente este MessageConverter al RabbitTemplate.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
