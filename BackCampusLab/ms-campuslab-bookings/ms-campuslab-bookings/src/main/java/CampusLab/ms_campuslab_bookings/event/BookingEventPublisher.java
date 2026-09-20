package CampusLab.ms_campuslab_bookings.event;

import CampusLab.ms_campuslab_bookings.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Publica en el tópico {@code bookings.events} (Kafka) en cada cambio de
 * estado de una reserva. Este tópico es la fuente de verdad consumida por
 * ms-campuslab-catalog (ajuste de stock), ms-campuslab-audit (timeline) y
 * ms-campuslab-report (KPIs) — ver sección 8 del brief.
 */
@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);

    // Object,Object porque es el tipo exacto que autoconfigura Spring Boot para
    // el KafkaTemplate por defecto (a partir de spring.kafka.producer.*); un
    // KafkaTemplate<String,Object> aquí no matchea por invarianza de genéricos
    // y falla el arranque con "No qualifying bean of type KafkaTemplate".
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topic;

    public BookingEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate,
                                  @Value("${app.kafka.topics.bookings-events:bookings.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publica el evento de dominio para el estado actual de la reserva.
     * type = "booking.<estado_en_minusculas>" (ej. booking.aprobada).
     */
    public void publishStatusChanged(Booking booking, UUID correlationId) {
        String type = "booking." + booking.getStatus().name().toLowerCase(Locale.ROOT);
        EventEnvelope<BookingEventPayload> envelope =
                EventEnvelope.of(type, BookingEventPayload.from(booking), correlationId);
        kafkaTemplate.send(topic, String.valueOf(booking.getId()), envelope);
        log.debug("Publicado en {}: type={} bookingId={} correlationId={}", topic, type, booking.getId(), correlationId);
    }
}
