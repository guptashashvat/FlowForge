package com.flowforge.web.dto;

import com.flowforge.domain.model.WorkflowStatus;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardResponse(
            long totalRequests,
            long pendingRequests,
            long inProgressRequests,
            long completedRequests,
            long rejectedRequests,
            List<StatusMetric> requestsByStatus,
            List<MonthlyMetric> requestsByMonth
    ) {
    }

    public record StatusMetric(
            WorkflowStatus status,
            String label,
            long total
    ) {
    }

    public record MonthlyMetric(
            String month,
            long total
    ) {
    }
}
