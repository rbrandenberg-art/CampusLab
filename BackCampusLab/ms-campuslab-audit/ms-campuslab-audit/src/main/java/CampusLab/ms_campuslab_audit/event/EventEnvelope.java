package CampusLab.ms_campuslab_audit.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope comun de eventos (Kafka y RabbitMQ) acordado entre todos los microservicios de
 * CampusLab. Ver seccion 4 de {@code docs/campuslab-brief.md}. Cada servicio declara su propia
 * copia minima; todavia no existe una libreria compartida entre microservicios.
 *
 * @param <T> tipo del payload especifico del dominio que publica el evento.
 */
public class EventEnvelope<T> {

    private String type;
    private UUID eventId;
    private Instant timestamp;
    private String traceId;
    private String correlationId;
    private T payload;

    public EventEnvelope() {
    }

    public EventEnvelope(String type, UUID eventId, Instant timestamp, String traceId,
            String correlationId, T payload) {
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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
