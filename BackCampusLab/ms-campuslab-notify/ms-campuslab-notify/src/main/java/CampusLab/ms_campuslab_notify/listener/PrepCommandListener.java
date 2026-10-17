package CampusLab.ms_campuslab_notify.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;

import CampusLab.ms_campuslab_notify.config.RabbitQueues;
import CampusLab.ms_campuslab_notify.event.EventEnvelope;
import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.requireValid;
import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.textOrDefault;

@Component
public class PrepCommandListener {

    private static final Logger log = LoggerFactory.getLogger(PrepCommandListener.class);

    @RabbitListener(queues = RabbitQueues.PREP)
    public void onPrepCommand(EventEnvelope envelope,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            requireValid(envelope, RabbitQueues.PREP);

            JsonNode payload = envelope.getPayload();
            String bookingId = textOrDefault(payload, "bookingId", "n/a");
            String recurso = textOrDefault(payload, "resourceId", textOrDefault(payload, "labId", "n/a"));
            String tecnico = textOrDefault(payload, "technicianId", "n/a");

            log.info("[NOTIFY][PREP] Ticket de preparación simulado -> tipo={} bookingId={} recurso={} tecnico={} eventId={} traceId={} payload={}",
                    envelope.getType(), bookingId, recurso, tecnico, envelope.getEventId(), envelope.getTraceId(), payload);

            // ÉXITO: Confirmación manual
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("[NOTIFY][PREP] Error procesando ticket. Enviando a DLQ. Motivo: {}", e.getMessage());
            // FALLO: Envío a DLQ
            channel.basicNack(tag, false, false);
        }
    }
}