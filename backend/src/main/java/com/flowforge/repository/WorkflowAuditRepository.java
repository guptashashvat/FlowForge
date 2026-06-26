package com.flowforge.repository;

import com.flowforge.domain.entity.WorkflowAudit;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowAuditRepository extends JpaRepository<WorkflowAudit, Long> {
    @EntityGraph(attributePaths = {"user", "user.roles"})
    List<WorkflowAudit> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
