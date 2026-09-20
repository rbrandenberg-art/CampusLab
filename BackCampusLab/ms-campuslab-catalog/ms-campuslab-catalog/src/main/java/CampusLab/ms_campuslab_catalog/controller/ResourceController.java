package CampusLab.ms_campuslab_catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import CampusLab.ms_campuslab_catalog.domain.ResourceType;
import CampusLab.ms_campuslab_catalog.dto.CreateResourceRequest;
import CampusLab.ms_campuslab_catalog.dto.ResourceResponse;
import CampusLab.ms_campuslab_catalog.dto.UpdateResourceRequest;
import CampusLab.ms_campuslab_catalog.service.ResourceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<ResourceResponse> list(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Boolean active) {
        return resourceService.findResources(type, active);
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody CreateResourceRequest request) {
        ResourceResponse created = resourceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateResourceRequest request) {
        return resourceService.update(id, request);
    }
}
