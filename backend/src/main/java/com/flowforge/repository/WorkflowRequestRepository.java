package com.flowforge.repository;

import com.flowforge.domain.entity.WorkflowRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkflowRequestRepository extends JpaRepository<WorkflowRequest, Long>, JpaSpecificationExecutor<WorkflowRequest> {
    @Override
    @EntityGraph(attributePaths = {"createdBy", "createdBy.roles", "assignedTo", "assignedTo.roles"})
    Optional<WorkflowRequest> findById(Long id);
}
