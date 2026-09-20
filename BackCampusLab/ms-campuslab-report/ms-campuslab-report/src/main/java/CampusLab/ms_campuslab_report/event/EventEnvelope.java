package CampusLab.ms_campuslab_report.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope comun de eventos (Kafka/RabbitMQ) compartido entre los microservicios de CampusLab.
 * Cada microservicio declara su propia copia minima (aun no existe una libreria compartida).
 *
 * @param <T> tipo del payload especifico del evento
 */
public class EventEnvelope<T> {

    private String type;
    private UUID eventId;
    private Instant timestamp;
    private UUID traceId;
    private UUID correlationId;
    private T payload;

    public EventEnvelope() {
    }

    public EventEnvelope(String type, UUID eventId, Instant timestamp, UUID traceId, UUID correlationId, T payload) {
        this.type = type;
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.payload = payload;
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

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
