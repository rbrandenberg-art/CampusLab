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
public class VoucherCommandListener {

    private static final Logger log = LoggerFactory.getLogger(VoucherCommandListener.class);

    @RabbitListener(queues = RabbitQueues.VOUCHER)
    public void onVoucherCommand(EventEnvelope envelope,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            requireValid(envelope, RabbitQueues.VOUCHER);

            JsonNode payload = envelope.getPayload();
            String bookingId = textOrDefault(payload, "bookingId", "n/a");
            String tipoDocumento = describeDocument(envelope.getType());

            log.info("[NOTIFY][VOUCHER] PDF simulado -> documento={} tipoEvento={} bookingId={} eventId={} traceId={} payload={}",
                    tipoDocumento, envelope.getType(), bookingId, envelope.getEventId(), envelope.getTraceId(), payload);

            // ÉXITO: Confirmación manual
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("[NOTIFY][VOUCHER] Error procesando PDF. Enviando a DLQ. Motivo: {}", e.getMessage());
            // FALLO: Envío a DLQ
            channel.basicNack(tag, false, false);
        }
    }

    private String describeDocument(String eventType) {
        if (eventType != null && eventType.toLowerCase().contains("return")) {
            return "Acta de devolución";
        }
        return "Vale de retiro";
    }
}