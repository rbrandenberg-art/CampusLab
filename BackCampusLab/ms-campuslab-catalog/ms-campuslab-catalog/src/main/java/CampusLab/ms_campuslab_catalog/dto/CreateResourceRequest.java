package CampusLab.ms_campuslab_catalog.dto;

import CampusLab.ms_campuslab_catalog.domain.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload de creación de un recurso. totalStock define el cupo inicial;
 * availableStock inicial se fija igual a totalStock (regla del enunciado).
 */
public record CreateResourceRequest(
        @NotBlank String name,
        @NotNull ResourceType type,
        String description,
        String location,
        @Min(0) int totalStock,
        Boolean active
) {
}
