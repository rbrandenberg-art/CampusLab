package CampusLab.ms_campuslab_catalog.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import CampusLab.ms_campuslab_catalog.service.ResourceService;

/**
 * Consumidor del tópico {@code bookings.events} (definido por
 * ms-campuslab-bookings). Implementa la regla de negocio: el cupo del
 * recurso disminuye al APROBAR la reserva (no al solicitarla) y se
 * restituye al devolverla o cancelarla.
 */
@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private static final String EVENT_APROBADA = "booking.aprobada";
    private static final String EVENT_DEVUELTA = "booking.devuelta";
    private static final String EVENT_CANCELADA = "booking.cancelada";

    private final ResourceService resourceService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(ResourceService resourceService, ObjectMapper objectMapper) {
        this.resourceService = resourceService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.bookings-events:bookings.events}",
            groupId = "${spring.kafka.consumer.group-id:catalog-service}"
    )
    public void onBookingEvent(String message) {
        EventEnvelope<BookingEventPayload> envelope;
        try {
            envelope = objectMapper.readValue(message, new TypeReference<EventEnvelope<BookingEventPayload>>() {
            });
        } catch (Exception e) {
            log.error("No se pudo deserializar el evento de bookings.events: {}", message, e);
            return;
        }

        if (envelope == null || envelope.type() == null || envelope.payload() == null) {
            log.warn("Evento de bookings.events incompleto, se ignora: {}", message);
            return;
        }

        Long resourceId = envelope.payload().resourceId();
        if (resourceId == null) {
            log.warn("Evento {} sin resourceId en el payload, se ignora", envelope.type());
            return;
        }

        switch (envelope.type()) {
            case EVENT_APROBADA -> {
                resourceService.decrementAvailableStock(resourceId);
                log.info("Cupo decrementado para resourceId={} por evento {}", resourceId, envelope.type());
            }
            case EVENT_DEVUELTA, EVENT_CANCELADA -> {
                resourceService.incrementAvailableStock(resourceId);
                log.info("Cupo incrementado para resourceId={} por evento {}", resourceId, envelope.type());
            }
            default -> log.debug("Evento {} no requiere ajuste de cupo, se ignora", envelope.type());
        }
    }
}
