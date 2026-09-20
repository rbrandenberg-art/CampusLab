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
 * Consume q.cmd.voucher: generación de PDF (vale de retiro o acta de
 * devolución) — bindings voucher.gen / voucher.*.
 * <p>
 * Aún no existe un generador de PDF real: se simula indicando qué documento
 * se "generaría" y para qué reserva, con un log de nivel INFO.
 */
@Component
public class VoucherCommandListener {

    private static final Logger log = LoggerFactory.getLogger(VoucherCommandListener.class);

    @RabbitListener(queues = RabbitQueues.VOUCHER)
    public void onVoucherCommand(EventEnvelope envelope) {
        requireValid(envelope, RabbitQueues.VOUCHER);

        JsonNode payload = envelope.getPayload();
        String bookingId = textOrDefault(payload, "bookingId", "n/a");
        String tipoDocumento = describeDocument(envelope.getType());

        log.info("[NOTIFY][VOUCHER] PDF simulado -> documento={} tipoEvento={} bookingId={} eventId={} traceId={} payload={}",
                tipoDocumento, envelope.getType(), bookingId, envelope.getEventId(), envelope.getTraceId(), payload);
    }

    private String describeDocument(String eventType) {
        if (eventType != null && eventType.toLowerCase().contains("return")) {
            return "Acta de devolución";
        }
        return "Vale de retiro";
    }
}
