package CampusLab.ms_campuslab_audit.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Reintentos y dead-letter para el consumidor de {@code bookings.events}, siguiendo la
 * convencion {@code *.DLT (por consumidor)} de la seccion 8 del brief: 3 reintentos y luego
 * publicacion en {@code bookings.events.DLT} con metadatos del error.
 */
@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            // Object,Object: mismo motivo que en AuditTimelinePublisher — el
            // KafkaTemplate autoconfigurado por Spring Boot es <Object,Object>.
            KafkaOperations<Object, Object> kafkaOperations,
            @Value("${app.kafka.topics.bookings-events-dlt:bookings.events.DLT}") String dltTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(dltTopic, record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        errorHandler.addNotRetryableExceptions(com.fasterxml.jackson.core.JsonProcessingException.class);
        return errorHandler;
    }
}
