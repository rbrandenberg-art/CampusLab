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
 * Consume q.cmd.email: notificaciones email/push al estudiante (aprobación,
 * sala lista, devolución, etc. — bindings email.send / email.*).
 * <p>
 * No existe todavía un proveedor de email real: se simula el envío dejando
 * constancia en el log de nivel INFO.
 */
@Component
public class EmailCommandListener {

    private static final Logger log = LoggerFactory.getLogger(EmailCommandListener.class);

    @RabbitListener(queues = RabbitQueues.EMAIL)
    public void onEmailCommand(EventEnvelope envelope) {
        requireValid(envelope, RabbitQueues.EMAIL);

        JsonNode payload = envelope.getPayload();
        String destinatario = textOrDefault(payload, "studentEmail", textOrDefault(payload, "email", "desconocido"));
        String bookingId = textOrDefault(payload, "bookingId", "n/a");

        log.info("[NOTIFY][EMAIL] Envío simulado -> tipo={} destinatario={} bookingId={} eventId={} traceId={} payload={}",
                envelope.getType(), destinatario, bookingId, envelope.getEventId(), envelope.getTraceId(), payload);
    }
}
