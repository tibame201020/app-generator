package com.tibame.app_generator.repository;

import com.tibame.app_generator.model.ContainerInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContainerInstanceRepository extends JpaRepository<ContainerInstance, UUID> {
    Optional<ContainerInstance> findByProjectId(UUID projectId);
    Optional<ContainerInstance> findBySubdomain(String subdomain);
}
