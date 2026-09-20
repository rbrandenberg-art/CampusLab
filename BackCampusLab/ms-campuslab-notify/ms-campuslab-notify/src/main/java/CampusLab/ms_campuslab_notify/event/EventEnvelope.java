package CampusLab.ms_campuslab_notify.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope común de eventos usado por todos los microservicios de CampusLab
 * (ver sección 4 del brief compartido). Cada servicio declara su propia copia
 * mínima: aún no existe una librería compartida entre microservicios.
 *
 * <pre>
 * {
 *   "type": "string (ej. email.approved)",
 *   "eventId": "UUID",
 *   "timestamp": "ISO-8601",
 *   "traceId": "UUID",
 *   "correlationId": "UUID",
 *   "payload": { }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventEnvelope {

    private String type;
    private UUID eventId;
    private Instant timestamp;
    private UUID traceId;
    private UUID correlationId;
    private JsonNode payload;

    public EventEnvelope() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "type='" + type + '\'' +
                ", eventId=" + eventId +
                ", timestamp=" + timestamp +
                ", traceId=" + traceId +
                ", correlationId=" + correlationId +
                ", payload=" + payload +
                '}';
    }
}
