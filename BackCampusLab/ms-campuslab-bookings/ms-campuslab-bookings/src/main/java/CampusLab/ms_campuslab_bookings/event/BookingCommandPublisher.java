package CampusLab.ms_campuslab_bookings.event;

import CampusLab.ms_campuslab_bookings.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Publica comandos en RabbitMQ (sección 7 del brief) para que
 * ms-campuslab-notify arme el ticket de preparación y envíe los emails al
 * estudiante. La topología (exchanges/colas/DLQ) la administra
 * ms-campuslab-rabbitmq; este servicio solo publica contra los exchanges ya
 * acordados, sin declarar colas ni bindings.
 */
@Component
public class BookingCommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingCommandPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeDirect;
    private final String exchangeTopic;

    public BookingCommandPublisher(RabbitTemplate rabbitTemplate,
                                    @Value("${app.rabbitmq.exchanges.cmd-direct:cmd.direct}") String exchangeDirect,
                                    @Value("${app.rabbitmq.exchanges.cmd-topic:cmd.topic}") String exchangeTopic) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeDirect = exchangeDirect;
        this.exchangeTopic = exchangeTopic;
    }

    /** Al aprobar: ticket de preparación de sala/equipo para el técnico (binding directo prep.ticket). */
    public void publishPrepTicket(Booking booking, UUID correlationId) {
        send(exchangeDirect, "prep.ticket", booking, correlationId);
    }

    /** Al aprobar: email al estudiante avisando la aprobación (binding topic email.*). */
    public void publishEmailApproved(Booking booking, UUID correlationId) {
        send(exchangeTopic, "email.approved", booking, correlationId);
    }

    /** Al devolver: email al estudiante confirmando la devolución (binding topic email.*). */
    public void publishEmailReturned(Booking booking, UUID correlationId) {
        send(exchangeTopic, "email.returned", booking, correlationId);
    }

    private void send(String exchange, String routingKey, Booking booking, UUID correlationId) {
        String type = "booking." + booking.getStatus().name().toLowerCase(Locale.ROOT);
        EventEnvelope<BookingEventPayload> envelope =
                EventEnvelope.of(type, BookingEventPayload.from(booking), correlationId);
        rabbitTemplate.convertAndSend(exchange, routingKey, envelope);
        log.debug("Publicado en exchange={} routingKey={} bookingId={} correlationId={}",
                exchange, routingKey, booking.getId(), correlationId);
    }
}
