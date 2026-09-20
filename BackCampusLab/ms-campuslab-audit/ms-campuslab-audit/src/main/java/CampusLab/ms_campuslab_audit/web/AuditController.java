package CampusLab.ms_campuslab_audit.web;

import CampusLab.ms_campuslab_audit.dto.AuditEventDto;
import CampusLab.ms_campuslab_audit.dto.AuditEventPageDto;
import CampusLab.ms_campuslab_audit.service.AuditEventService;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de solo lectura del timeline de auditoria. No existe ningun endpoint de escritura:
 * los registros solo se crean desde el consumidor de {@code bookings.events}.
 */
@RestController
public class AuditController {

    private final AuditEventService auditEventService;

    public AuditController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping("/api/audit/events")
    public AuditEventPageDto getEvents(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {
        return AuditEventPageDto.from(
                auditEventService.findEvents(eventType, bookingId, from, to, pageable));
    }

    @GetMapping("/api/audit/events/{id}")
    public ResponseEntity<AuditEventDto> getEvent(@PathVariable Long id) {
        return auditEventService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
