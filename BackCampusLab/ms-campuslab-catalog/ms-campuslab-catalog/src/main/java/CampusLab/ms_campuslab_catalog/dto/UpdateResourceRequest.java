package CampusLab.ms_campuslab_catalog.dto;

import CampusLab.ms_campuslab_catalog.domain.ResourceType;
import jakarta.validation.constraints.Min;

/**
 * Payload de actualización parcial. Todos los campos son opcionales:
 * solo se aplican los que vienen no nulos (permite actualizar solo
 * cupo/stock u otros campos editables sin pisar el resto).
 */
public record UpdateResourceRequest(
        String name,
        ResourceType type,
        String description,
        String location,
        @Min(0) Integer totalStock,
        @Min(0) Integer availableStock,
        Boolean active
) {
}
