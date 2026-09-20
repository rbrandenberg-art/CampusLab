package CampusLab.ms_campuslab_notify.listener;

import CampusLab.ms_campuslab_notify.config.RabbitQueues;
import CampusLab.ms_campuslab_notify.event.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.requireValid;
import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.textOrDefault;

/**
 * Consume q.cmd.prep: ticket de preparación de sala/equipo para el técnico
 * (bindings prep.ticket / prep.#).
 * <p>
 * Aún no existe integración con un sistema real de tickets: se simula la
 * creación dejando constancia en el log de nivel INFO.
 */
@Component
public class PrepCommandListener {

    private static final Logger log = LoggerFactory.getLogger(PrepCommandListener.class);

    @RabbitListener(queues = RabbitQueues.PREP)
    public void onPrepCommand(EventEnvelope envelope) {
        requireValid(envelope, RabbitQueues.PREP);

        JsonNode payload = envelope.getPayload();
        String bookingId = textOrDefault(payload, "bookingId", "n/a");
        String recurso = textOrDefault(payload, "resourceId", textOrDefault(payload, "labId", "n/a"));
        String tecnico = textOrDefault(payload, "technicianId", "n/a");

        log.info("[NOTIFY][PREP] Ticket de preparación simulado -> tipo={} bookingId={} recurso={} tecnico={} eventId={} traceId={} payload={}",
                envelope.getType(), bookingId, recurso, tecnico, envelope.getEventId(), envelope.getTraceId(), payload);
    }
}
