package CampusLab.ms_campuslab_report.kafka;

import CampusLab.ms_campuslab_report.domain.BookingFact;
import CampusLab.ms_campuslab_report.event.EventEnvelope;
import CampusLab.ms_campuslab_report.repository.BookingFactRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

/**
 * Consume el topico {@code bookings.events} (fuente de verdad de eventos de la reserva,
 * publicado por ms-campuslab-bookings) y proyecta cada evento {@code booking.*} como un
 * {@link BookingFact} para poder calcular KPIs con consultas simples.
 *
 * No se hace deduplicacion sofisticada: cada mensaje recibido inserta un nuevo hecho
 * (caso academico).
 */
@Component
public class BookingEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventsConsumer.class);
    private static final String BOOKING_TYPE_PREFIX = "booking.";

    private final BookingFactRepository bookingFactRepository;
    private final ObjectMapper objectMapper;

    public BookingEventsConsumer(BookingFactRepository bookingFactRepository, ObjectMapper objectMapper) {
        this.bookingFactRepository = bookingFactRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${campuslab.kafka.topic.bookings-events:bookings.events}", groupId = "report-service")
    public void onBookingEvent(String message) {
        EventEnvelope<Map<String, Object>> envelope;
        try {
            envelope = objectMapper.readValue(message, new TypeReference<EventEnvelope<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.error("No se pudo deserializar el evento recibido de bookings.events: {}", message, e);
            return;
        }

        String type = envelope.getType();
        if (type == null || !type.startsWith(BOOKING_TYPE_PREFIX)) {
            return; // no es un evento de dominio booking.*, se ignora
        }

        Map<String, Object> payload = envelope.getPayload();
        Long bookingId = extractLong(payload, "bookingId", "booking_id", "id");
        Long resourceId = extractLong(payload, "resourceId", "resource_id");
        String status = extractStatus(payload, type);
        Instant occurredAt = envelope.getTimestamp() != null ? envelope.getTimestamp() : Instant.now();

        BookingFact fact = new BookingFact(bookingId, resourceId, status, occurredAt);
        bookingFactRepository.save(fact);
        log.debug("BookingFact registrado: bookingId={}, resourceId={}, status={}, occurredAt={}",
                bookingId, resourceId, status, occurredAt);
    }

    private String extractStatus(Map<String, Object> payload, String type) {
        if (payload != null && payload.get("status") != null) {
            return String.valueOf(payload.get("status")).toUpperCase(Locale.ROOT);
        }
        // Se deriva del sufijo del type (ej. booking.solicitada -> SOLICITADA)
        return type.substring(BOOKING_TYPE_PREFIX.length()).toUpperCase(Locale.ROOT);
    }

    private Long extractLong(Map<String, Object> payload, String... keys) {
        if (payload == null) {
            return null;
        }
        for (String key : keys) {
            Object value = payload.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number number) {
                return number.longValue();
            }
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // sigue con la siguiente clave candidata
            }
        }
        return null;
    }
}
