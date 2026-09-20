package CampusLab.ms_campuslab_audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro de auditoria normalizado a partir de un {@code EventEnvelope} consumido desde el
 * topico Kafka {@code bookings.events}. Es la unica forma de crear filas en esta tabla: no existe
 * ningun endpoint de escritura.
 */
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "actor_id", length = 150)
    private String actorId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;

    protected AuditEvent() {
        // JPA
    }

    public AuditEvent(String eventType, Long bookingId, Long resourceId, String actorId,
            Instant occurredAt, String traceId, String correlationId, String rawPayload) {
        this.eventType = eventType;
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.actorId = actorId;
        this.occurredAt = occurredAt;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.rawPayload = rawPayload;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getActorId() {
        return actorId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRawPayload() {
        return rawPayload;
    }
}
