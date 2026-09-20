package CampusLab.ms_campuslab_audit.repository;

import CampusLab.ms_campuslab_audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de solo lectura desde la perspectiva de la API publica: las unicas escrituras las
 * realiza {@code AuditEventService} al consumir eventos de Kafka.
 */
public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {
}
