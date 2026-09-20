package CampusLab.ms_campuslab_notify.listener;

import CampusLab.ms_campuslab_notify.event.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utilidades compartidas por los listeners de q.cmd.email/prep/voucher para
 * validar el envelope recibido y leer campos del payload de forma defensiva.
 */
final class EventEnvelopeSupport {

    private EventEnvelopeSupport() {
    }

    /**
     * Si el envelope llega inválido (nulo, sin type o sin payload) se lanza la
     * excepción para que el broker haga NACK/reintente según la política de
     * DLQ ya definida por el administrador de RabbitMQ. Este servicio no
     * implementa lógica propia de reintentos ni de colas DLQ.
     */
    static void requireValid(EventEnvelope envelope, String queueName) {
        if (envelope == null) {
            throw new IllegalArgumentException("Mensaje vacío en " + queueName);
        }
        if (envelope.getType() == null || envelope.getType().isBlank()) {
            throw new IllegalArgumentException("EventEnvelope inválido en " + queueName + ": falta 'type'");
        }
        if (envelope.getPayload() == null || envelope.getPayload().isNull()) {
            throw new IllegalArgumentException("EventEnvelope inválido en " + queueName + ": falta 'payload'");
        }
    }

    static String textOrDefault(JsonNode payload, String field, String defaultValue) {
        if (payload != null && payload.hasNonNull(field)) {
            return payload.get(field).asText();
        }
        return defaultValue;
    }
}
