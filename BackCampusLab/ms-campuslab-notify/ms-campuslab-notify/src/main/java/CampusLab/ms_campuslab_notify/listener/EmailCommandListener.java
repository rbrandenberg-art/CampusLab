package CampusLab.ms_campuslab_notify.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener; // Importar Channel de RabbitMQ
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode; // Importar cabeceras
import com.rabbitmq.client.Channel; // Importar Header

import CampusLab.ms_campuslab_notify.config.RabbitQueues;
import CampusLab.ms_campuslab_notify.event.EventEnvelope;
import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.requireValid;
import static CampusLab.ms_campuslab_notify.listener.EventEnvelopeSupport.textOrDefault;

@Component
public class EmailCommandListener {

    private static final Logger log = LoggerFactory.getLogger(EmailCommandListener.class);

    @RabbitListener(queues = RabbitQueues.EMAIL)
    public void onEmailCommand(EventEnvelope envelope, 
                               Channel channel, 
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            requireValid(envelope, RabbitQueues.EMAIL);

            JsonNode payload = envelope.getPayload();
            String destinatario = textOrDefault(payload, "studentEmail", textOrDefault(payload, "email", "desconocido"));
            String bookingId = textOrDefault(payload, "bookingId", "n/a");

            // TODO: (Opcional) Si quieres probar la DLQ, lanza una excepción intencionalmente aquí.
            // if (destinatario.equals("desconocido")) throw new RuntimeException("Email inválido");

            log.info("[NOTIFY][EMAIL] Envío simulado -> tipo={} destinatario={} bookingId={} eventId={} traceId={} payload={}",
                    envelope.getType(), destinatario, bookingId, envelope.getEventId(), envelope.getTraceId(), payload);
            
            // ÉXITO: Confirmamos a RabbitMQ que procesamos el mensaje
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("[NOTIFY][EMAIL] Error procesando mensaje. Enviando a DLQ. Motivo: {}", e.getMessage());
            // FALLO: Rechazamos el mensaje. El false final (requeue=false) hace que RabbitMQ 
            // lo envíe automáticamente a la Dead Letter Exchange (cmd.dead.dlx) que configuraste.
            channel.basicNack(tag, false, false);
        }
    }
}