package CampusLab.ms_campuslab_audit.service;

import CampusLab.ms_campuslab_audit.domain.AuditEvent;
import CampusLab.ms_campuslab_audit.dto.AuditEventDto;
import CampusLab.ms_campuslab_audit.event.EventEnvelope;
import CampusLab.ms_campuslab_audit.kafka.AuditTimelinePublisher;
import CampusLab.ms_campuslab_audit.repository.AuditEventRepository;
import CampusLab.ms_campuslab_audit.repository.AuditEventSpecifications;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Normaliza y persiste los eventos de {@code bookings.events}, republica en
 * {@code audit.timeline} y resuelve las consultas de solo lectura de {@code /api/audit/*}.
 * La unica via de escritura es {@link #ingest(EventEnvelope)}, invocado desde el listener Kafka.
 */
@Service
public class AuditEventService {

    /** Nombres de campo candidatos dentro del payload para cada dato normalizado. */
    private static final List<String> BOOKING_ID_FIELDS = List.of("bookingId", "reservaId");
    private static final List<String> RESOURCE_ID_FIELDS = List.of("resourceId", "recursoId", "equipmentId");
    private static final List<String> ACTOR_ID_FIELDS =
            List.of("actorId", "userId", "studentId", "technicianId", "requestedBy", "approvedBy");

    private final AuditEventRepository repository;
    private final AuditTimelinePublisher timelinePublisher;
    private final ObjectMapper objectMapper;

    public AuditEventService(AuditEventRepository repository, AuditTimelinePublisher timelinePublisher,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.timelinePublisher = timelinePublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditEvent ingest(EventEnvelope<JsonNode> envelope) {
        JsonNode payload = envelope.getPayload();

        Long bookingId = firstLong(payload, BOOKING_ID_FIELDS);
        Long resourceId = firstLong(payload, RESOURCE_ID_FIELDS);
        String actorId = firstText(payload, ACTOR_ID_FIELDS);
        Instant occurredAt = envelope.getTimestamp() != null ? envelope.getTimestamp() : Instant.now();
        String rawPayload = toRawJson(payload);

        AuditEvent event = new AuditEvent(
                envelope.getType(),
                bookingId,
                resourceId,
                actorId,
                occurredAt,
                envelope.getTraceId(),
                envelope.getCorrelationId(),
                rawPayload);

        AuditEvent saved = repository.save(event);
        timelinePublisher.publish(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDto> findEvents(String eventType, Long bookingId, Instant from, Instant to,
            Pageable pageable) {
        return repository.findAll(AuditEventSpecifications.withFilters(eventType, bookingId, from, to), pageable)
                .map(AuditEventDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<AuditEventDto> findById(Long id) {
        return repository.findById(id).map(AuditEventDto::from);
    }

    private String toRawJson(JsonNode payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return payload.toString();
        }
    }

    private Long firstLong(JsonNode payload, List<String> fieldNames) {
        if (payload == null) {
            return null;
        }
        for (String field : fieldNames) {
            JsonNode node = payload.get(field);
            if (node != null && !node.isNull() && node.canConvertToLong()) {
                return node.asLong();
            }
        }
        return null;
    }

    private String firstText(JsonNode payload, List<String> fieldNames) {
        if (payload == null) {
            return null;
        }
        for (String field : fieldNames) {
            JsonNode node = payload.get(field);
            if (node != null && !node.isNull()) {
                return node.asText();
            }
        }
        return null;
    }
}
