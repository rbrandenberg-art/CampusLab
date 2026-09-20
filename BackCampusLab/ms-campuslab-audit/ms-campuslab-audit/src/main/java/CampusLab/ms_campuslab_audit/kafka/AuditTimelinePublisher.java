package CampusLab.ms_campuslab_audit.kafka;

import CampusLab.ms_campuslab_audit.domain.AuditEvent;
import CampusLab.ms_campuslab_audit.event.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Republica en {@code audit.timeline} el evento ya normalizado y persistido. Este servicio de
 * auditoria es el dueno logico de ese topico: alimenta el historico "quien/que/cuando/desde
 * donde" (seccion 8 del brief).
 */
@Component
public class AuditTimelinePublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditTimelinePublisher.class);

    // Object,Object porque es el tipo exacto que autoconfigura Spring Boot para
    // el KafkaTemplate por defecto; un KafkaTemplate<String,String> aquí no
    // matchea por invarianza de genéricos y falla el arranque ("No qualifying
    // bean of type KafkaTemplate").
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String auditTimelineTopic;

    public AuditTimelinePublisher(KafkaTemplate<Object, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.audit-timeline:audit.timeline}") String auditTimelineTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.auditTimelineTopic = auditTimelineTopic;
    }

    public void publish(AuditEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("auditEventId", event.getId());
        payload.put("bookingId", event.getBookingId());
        payload.put("resourceId", event.getResourceId());
        payload.put("actorId", event.getActorId());

        EventEnvelope<Map<String, Object>> envelope = new EventEnvelope<>(
                event.getEventType(),
                UUID.randomUUID(),
                event.getOccurredAt(),
                event.getTraceId(),
                event.getCorrelationId(),
                payload);

        try {
            String json = objectMapper.writeValueAsString(envelope);
            String key = event.getBookingId() != null
                    ? String.valueOf(event.getBookingId())
                    : event.getTraceId();
            kafkaTemplate.send(auditTimelineTopic, key, json);
        } catch (Exception ex) {
            log.error("No se pudo republicar el evento {} en {}: {}", event.getId(),
                    auditTimelineTopic, ex.getMessage(), ex);
        }
    }
}
