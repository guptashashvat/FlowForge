package com.flowforge.web.dto;

import com.flowforge.domain.model.WorkflowAction;
import com.flowforge.domain.model.WorkflowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class WorkflowDtos {
    private WorkflowDtos() {
    }

    public record CreateWorkflowRequest(
            @NotBlank @Size(max = 180) String title,
            @Size(max = 4000) String description,
            Long assignedToId
    ) {
    }

    public record UpdateWorkflowRequest(
            @NotBlank @Size(max = 180) String title,
            @Size(max = 4000) String description,
            Long assignedToId
    ) {
    }

    public record WorkflowActionRequest(@Size(max = 2000) String comments) {
    }

    public record WorkflowSummaryResponse(
            Long id,
            String title,
            WorkflowStatus status,
            String statusLabel,
            UserDtos.UserResponse createdBy,
            UserDtos.UserResponse assignedTo,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record WorkflowDetailResponse(
            Long id,
            String title,
            String description,
            WorkflowStatus status,
            String statusLabel,
            UserDtos.UserResponse createdBy,
            UserDtos.UserResponse assignedTo,
            Instant createdAt,
            Instant updatedAt,
            List<String> availableActions,
            List<AuditResponse> auditHistory
    ) {
    }

    public record AuditResponse(
            Long id,
            Instant timestamp,
            UserDtos.UserResponse user,
            WorkflowAction action,
            WorkflowStatus previousStatus,
            WorkflowStatus newStatus,
            String comments
    ) {
    }
}
