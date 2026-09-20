package CampusLab.ms_campuslab_catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import CampusLab.ms_campuslab_catalog.domain.Resource;
import CampusLab.ms_campuslab_catalog.domain.ResourceType;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByTypeAndActive(ResourceType type, boolean active);

    List<Resource> findByType(ResourceType type);

    List<Resource> findByActive(boolean active);
}
