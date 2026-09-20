package CampusLab.ms_campuslab_catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import CampusLab.ms_campuslab_catalog.domain.Resource;
import CampusLab.ms_campuslab_catalog.domain.ResourceType;
import CampusLab.ms_campuslab_catalog.dto.CreateResourceRequest;
import CampusLab.ms_campuslab_catalog.dto.ResourceResponse;
import CampusLab.ms_campuslab_catalog.dto.UpdateResourceRequest;
import CampusLab.ms_campuslab_catalog.repository.ResourceRepository;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> findResources(ResourceType type, Boolean active) {
        List<Resource> resources;
        if (type != null && active != null) {
            resources = resourceRepository.findByTypeAndActive(type, active);
        } else if (type != null) {
            resources = resourceRepository.findByType(type);
        } else if (active != null) {
            resources = resourceRepository.findByActive(active);
        } else {
            resources = resourceRepository.findAll();
        }
        return resources.stream().map(ResourceResponse::from).toList();
    }

    @Transactional
    public ResourceResponse create(CreateResourceRequest request) {
        Resource resource = new Resource(
                request.name(),
                request.type(),
                request.description(),
                request.location(),
                request.totalStock(),
                request.totalStock(), // availableStock inicial = totalStock
                request.active() == null || request.active()
        );
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, UpdateResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Recurso " + id + " no encontrado"));

        if (request.name() != null) {
            resource.setName(request.name());
        }
        if (request.type() != null) {
            resource.setType(request.type());
        }
        if (request.description() != null) {
            resource.setDescription(request.description());
        }
        if (request.location() != null) {
            resource.setLocation(request.location());
        }
        if (request.active() != null) {
            resource.setActive(request.active());
        }

        // totalStock/availableStock: se aplican explícitamente si vienen, y se
        // valida consistencia (availableStock nunca puede superar totalStock).
        Integer newTotal = request.totalStock();
        Integer newAvailable = request.availableStock();

        if (newTotal != null) {
            resource.setTotalStock(newTotal);
        }
        if (newAvailable != null) {
            resource.setAvailableStock(newAvailable);
        }
        if (resource.getAvailableStock() > resource.getTotalStock()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "availableStock no puede superar totalStock");
        }

        return ResourceResponse.from(resource);
    }

    /**
     * Decrementa en 1 el cupo disponible del recurso al aprobarse una reserva.
     * Consumido por el listener de Kafka. Si no queda cupo, no baja de 0.
     */
    @Transactional
    public void decrementAvailableStock(Long resourceId) {
        resourceRepository.findById(resourceId)
                .ifPresent(Resource::decrementAvailableStock);
    }

    /**
     * Incrementa en 1 el cupo disponible del recurso al devolverse o cancelarse
     * una reserva. No supera el totalStock.
     */
    @Transactional
    public void incrementAvailableStock(Long resourceId) {
        resourceRepository.findById(resourceId)
                .ifPresent(Resource::incrementAvailableStock);
    }
}
