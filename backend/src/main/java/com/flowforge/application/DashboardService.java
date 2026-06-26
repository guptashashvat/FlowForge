package com.flowforge.application;

import static com.flowforge.application.workflow.WorkflowSpecifications.visibleTo;

import com.flowforge.domain.entity.WorkflowRequest;
import com.flowforge.domain.model.WorkflowStatus;
import com.flowforge.repository.WorkflowRequestRepository;
import com.flowforge.security.UserPrincipal;
import com.flowforge.web.dto.DashboardDtos;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final WorkflowRequestRepository workflowRepository;

    public DashboardService(WorkflowRequestRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public DashboardDtos.DashboardResponse getDashboard(UserPrincipal principal) {
        Specification<WorkflowRequest> specification = Specification.where(visibleTo(principal));
        List<WorkflowRequest> requests = workflowRepository.findAll(specification);

        Map<WorkflowStatus, Long> byStatus = requests.stream()
                .collect(Collectors.groupingBy(WorkflowRequest::getStatus, Collectors.counting()));

        List<DashboardDtos.StatusMetric> statusMetrics = Arrays.stream(WorkflowStatus.values())
                .map(status -> new DashboardDtos.StatusMetric(status, status.label(), byStatus.getOrDefault(status, 0L)))
                .toList();

        List<DashboardDtos.MonthlyMetric> monthlyMetrics = monthlyMetrics(requests);

        long pending = requests.stream()
                .filter(request -> request.getAssignedTo() != null)
                .filter(request -> request.getAssignedTo().getId().equals(principal.getId()))
                .filter(request -> !request.getStatus().isTerminal())
                .count();
        long inProgress = byStatus.getOrDefault(WorkflowStatus.SUBMITTED, 0L)
                + byStatus.getOrDefault(WorkflowStatus.MANAGER_APPROVED, 0L)
                + byStatus.getOrDefault(WorkflowStatus.HR_APPROVED, 0L);

        return new DashboardDtos.DashboardResponse(
                requests.size(),
                pending,
                inProgress,
                byStatus.getOrDefault(WorkflowStatus.COMPLETED, 0L),
                byStatus.getOrDefault(WorkflowStatus.REJECTED, 0L),
                statusMetrics,
                monthlyMetrics
        );
    }

    private List<DashboardDtos.MonthlyMetric> monthlyMetrics(List<WorkflowRequest> requests) {
        YearMonth current = YearMonth.now(ZoneOffset.UTC);
        Map<YearMonth, Long> counts = requests.stream()
                .collect(Collectors.groupingBy(
                        request -> YearMonth.from(request.getCreatedAt().atZone(ZoneOffset.UTC)),
                        Collectors.counting()
                ));
        return IntStream.rangeClosed(0, 5)
                .mapToObj(index -> current.minusMonths(5L - index))
                .map(month -> new DashboardDtos.MonthlyMetric(month.toString(), counts.getOrDefault(month, 0L)))
                .toList();
    }
}
