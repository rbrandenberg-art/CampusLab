package CampusLab.ms_campuslab_catalog.dto;

import CampusLab.ms_campuslab_catalog.domain.Resource;
import CampusLab.ms_campuslab_catalog.domain.ResourceType;

/**
 * DTO de salida. No expone la entidad JPA directamente.
 */
public record ResourceResponse(
        Long id,
        String name,
        ResourceType type,
        String description,
        String location,
        int totalStock,
        int availableStock,
        boolean active
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getDescription(),
                resource.getLocation(),
                resource.getTotalStock(),
                resource.getAvailableStock(),
                resource.isActive()
        );
    }
}
