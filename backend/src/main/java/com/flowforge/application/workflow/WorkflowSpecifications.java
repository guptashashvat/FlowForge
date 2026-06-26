package com.flowforge.application.workflow;

import com.flowforge.domain.entity.WorkflowRequest;
import com.flowforge.domain.model.RoleName;
import com.flowforge.domain.model.WorkflowStatus;
import com.flowforge.security.UserPrincipal;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class WorkflowSpecifications {
    private WorkflowSpecifications() {
    }

    public static Specification<WorkflowRequest> visibleTo(UserPrincipal principal) {
        if (principal.hasRole(RoleName.MANAGER) || principal.hasRole(RoleName.HR_ADMIN)) {
            return (root, query, builder) -> builder.conjunction();
        }
        return (root, query, builder) -> builder.equal(root.get("createdBy").get("id"), principal.getId());
    }

    public static Specification<WorkflowRequest> statusEquals(WorkflowStatus status) {
        if (status == null) {
            return (root, query, builder) -> builder.conjunction();
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<WorkflowRequest> searchContains(String search) {
        if (!StringUtils.hasText(search)) {
            return (root, query, builder) -> builder.conjunction();
        }
        return (root, query, builder) -> {
            query.distinct(true);
            String term = "%" + search.trim().toLowerCase() + "%";
            var createdBy = root.join("createdBy", JoinType.LEFT);
            var assignedTo = root.join("assignedTo", JoinType.LEFT);
            return builder.or(
                    builder.like(builder.lower(root.get("title")), term),
                    builder.like(builder.lower(root.get("description")), term),
                    builder.like(builder.lower(createdBy.get("fullName")), term),
                    builder.like(builder.lower(assignedTo.get("fullName")), term)
            );
        };
    }
}
