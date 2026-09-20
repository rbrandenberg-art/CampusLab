package CampusLab.ms_campuslab_notify.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ms-campuslab-notify no declara colas/exchanges/DLQ: esa topología (sección 7
 * del brief) es responsabilidad de ms-campuslab-rabbitmq. Aquí solo se
 * configura cómo se deserializan los mensajes (envelope JSON común).
 * <p>
 * Se reutiliza el {@link ObjectMapper} autoconfigurado por Spring Boot (ya
 * trae el módulo JSR-310 para timestamps ISO-8601) en lugar de crear uno
 * nuevo. Spring Boot conecta automáticamente este {@link MessageConverter}
 * tanto al {@code RabbitTemplate} como al listener container factory por
 * defecto.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
