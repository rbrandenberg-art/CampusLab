package CampusLab.ms_campuslab_audit.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Envoltorio de paginacion propio para no serializar {@link org.springframework.data.domain.Page}
 * directamente (Spring recomienda no exponerlo tal cual en un controlador REST).
 */
public record AuditEventPageDto(
        List<AuditEventDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static AuditEventPageDto from(Page<AuditEventDto> page) {
        return new AuditEventPageDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
