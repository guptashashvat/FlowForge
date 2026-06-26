package com.flowforge.web;

import com.flowforge.application.WorkflowService;
import com.flowforge.domain.model.WorkflowStatus;
import com.flowforge.security.UserPrincipal;
import com.flowforge.web.dto.WorkflowDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public Page<WorkflowDtos.WorkflowSummaryResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) WorkflowStatus status,
            @RequestParam(defaultValue = "") String search
    ) {
        return workflowService.list(principal, page, size, sort, direction, status, search);
    }

    @GetMapping("/{requestId}")
    public WorkflowDtos.WorkflowDetailResponse getDetail(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.getDetail(requestId, principal);
    }

    @PostMapping
    public WorkflowDtos.WorkflowDetailResponse create(
            @Valid @RequestBody WorkflowDtos.CreateWorkflowRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.create(request, principal);
    }

    @PutMapping("/{requestId}")
    public WorkflowDtos.WorkflowDetailResponse updateDraft(
            @PathVariable Long requestId,
            @Valid @RequestBody WorkflowDtos.UpdateWorkflowRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.updateDraft(requestId, request, principal);
    }

    @PostMapping("/{requestId}/submit")
    public WorkflowDtos.WorkflowDetailResponse submit(
            @PathVariable Long requestId,
            @RequestBody(required = false) WorkflowDtos.WorkflowActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.submit(requestId, request, principal);
    }

    @PostMapping("/{requestId}/approve")
    public WorkflowDtos.WorkflowDetailResponse approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) WorkflowDtos.WorkflowActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.approve(requestId, request, principal);
    }

    @PostMapping("/{requestId}/reject")
    public WorkflowDtos.WorkflowDetailResponse reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) WorkflowDtos.WorkflowActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.reject(requestId, request, principal);
    }

    @PostMapping("/{requestId}/complete")
    public WorkflowDtos.WorkflowDetailResponse complete(
            @PathVariable Long requestId,
            @RequestBody(required = false) WorkflowDtos.WorkflowActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.complete(requestId, request, principal);
    }

    @PostMapping("/{requestId}/comments")
    public WorkflowDtos.WorkflowDetailResponse addComment(
            @PathVariable Long requestId,
            @Valid @RequestBody WorkflowDtos.WorkflowActionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return workflowService.addComment(requestId, request, principal);
    }
}
