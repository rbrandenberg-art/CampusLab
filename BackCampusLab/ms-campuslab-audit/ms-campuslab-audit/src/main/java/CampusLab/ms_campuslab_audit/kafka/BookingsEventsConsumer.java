package CampusLab.ms_campuslab_audit.kafka;

import CampusLab.ms_campuslab_audit.event.EventEnvelope;
import CampusLab.ms_campuslab_audit.service.AuditEventService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume el topico {@code bookings.events} (fuente de verdad de eventos de reserva, propiedad
 * del servicio de bookings) y persiste cada evento normalizado como {@code AuditEvent}.
 */
@Component
public class BookingsEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingsEventsConsumer.class);

    private final ObjectMapper objectMapper;
    private final AuditEventService auditEventService;

    public BookingsEventsConsumer(ObjectMapper objectMapper, AuditEventService auditEventService) {
        this.objectMapper = objectMapper;
        this.auditEventService = auditEventService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.bookings-events:bookings.events}",
            groupId = "${spring.kafka.consumer.group-id:audit-service}")
    public void onBookingEvent(String message) {
        try {
            EventEnvelope<JsonNode> envelope =
                    objectMapper.readValue(message, new TypeReference<EventEnvelope<JsonNode>>() {
                    });
            auditEventService.ingest(envelope);
        } catch (Exception ex) {
            // Se relanza para que el DefaultErrorHandler configurado reintente y, si sigue
            // fallando, publique en el DLT del consumidor (bookings.events.DLT).
            log.error("No se pudo procesar el mensaje de bookings.events: {}", message, ex);
            throw new IllegalStateException("Error procesando evento de bookings.events", ex);
        }
    }
}
