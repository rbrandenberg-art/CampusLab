package CampusLab.ms_campuslab_audit.repository;

import CampusLab.ms_campuslab_audit.domain.AuditEvent;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros opcionales para {@code GET /api/audit/events}: {@code eventType}, {@code bookingId} y
 * el rango {@code from}/{@code to} sobre {@code occurredAt}.
 */
public final class AuditEventSpecifications {

    private AuditEventSpecifications() {
    }

    public static Specification<AuditEvent> withFilters(String eventType, Long bookingId,
            Instant from, Instant to) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (eventType != null && !eventType.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("eventType"), eventType));
            }
            if (bookingId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("bookingId"), bookingId));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            }
            return predicate;
        };
    }
}
