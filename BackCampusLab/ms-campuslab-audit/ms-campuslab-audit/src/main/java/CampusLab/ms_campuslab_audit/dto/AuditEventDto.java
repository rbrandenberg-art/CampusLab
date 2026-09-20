package CampusLab.ms_campuslab_audit.dto;

import CampusLab.ms_campuslab_audit.domain.AuditEvent;
import java.time.Instant;

/**
 * Representacion de solo lectura de un {@link AuditEvent}, expuesta por {@code /api/audit/*}.
 * Nunca se expone la entidad JPA directamente.
 */
public record AuditEventDto(
        Long id,
        String eventType,
        Long bookingId,
        Long resourceId,
        String actorId,
        Instant occurredAt,
        String traceId,
        String correlationId,
        String rawPayload) {

    public static AuditEventDto from(AuditEvent entity) {
        return new AuditEventDto(
                entity.getId(),
                entity.getEventType(),
                entity.getBookingId(),
                entity.getResourceId(),
                entity.getActorId(),
                entity.getOccurredAt(),
                entity.getTraceId(),
                entity.getCorrelationId(),
                entity.getRawPayload());
    }
}
