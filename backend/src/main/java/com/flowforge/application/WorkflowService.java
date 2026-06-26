package com.flowforge.application;

import static com.flowforge.application.workflow.WorkflowSpecifications.searchContains;
import static com.flowforge.application.workflow.WorkflowSpecifications.statusEquals;
import static com.flowforge.application.workflow.WorkflowSpecifications.visibleTo;

import com.flowforge.domain.entity.AppUser;
import com.flowforge.domain.entity.WorkflowAudit;
import com.flowforge.domain.entity.WorkflowRequest;
import com.flowforge.domain.model.RoleName;
import com.flowforge.domain.model.WorkflowAction;
import com.flowforge.domain.model.WorkflowStatus;
import com.flowforge.repository.AppUserRepository;
import com.flowforge.repository.WorkflowAuditRepository;
import com.flowforge.repository.WorkflowRequestRepository;
import com.flowforge.security.UserPrincipal;
import com.flowforge.web.dto.WorkflowDtos;
import com.flowforge.web.error.ApiException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRequestRepository workflowRepository;
    private final WorkflowAuditRepository auditRepository;
    private final AppUserRepository userRepository;
    private final UserService userService;

    public WorkflowService(
            WorkflowRequestRepository workflowRepository,
            WorkflowAuditRepository auditRepository,
            AppUserRepository userRepository,
            UserService userService
    ) {
        this.workflowRepository = workflowRepository;
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<WorkflowDtos.WorkflowSummaryResponse> list(
            UserPrincipal principal,
            int page,
            int size,
            String sort,
            String direction,
            WorkflowStatus status,
            String search
    ) {
        Specification<WorkflowRequest> specification = Specification
                .where(visibleTo(principal))
                .and(statusEquals(status))
                .and(searchContains(search));
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sortDirection, safeSort(sort)));
        return workflowRepository.findAll(specification, pageRequest).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public WorkflowDtos.WorkflowDetailResponse getDetail(Long requestId, UserPrincipal principal) {
        WorkflowRequest request = getVisibleRequest(requestId, principal);
        return toDetail(request, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse create(WorkflowDtos.CreateWorkflowRequest request, UserPrincipal principal) {
        AppUser actor = getActor(principal);
        AppUser assignee = resolveAssignee(request.assignedToId(), RoleName.MANAGER);
        WorkflowRequest workflow = new WorkflowRequest(request.title().trim(), clean(request.description()), actor, assignee);
        WorkflowRequest saved = workflowRepository.save(workflow);
        audit(saved, actor, WorkflowAction.CREATE, null, WorkflowStatus.DRAFT, "Draft request created.");
        return toDetail(saved, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse updateDraft(Long requestId, WorkflowDtos.UpdateWorkflowRequest request, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        ensureOwner(workflow, principal);
        ensureStatus(workflow, WorkflowStatus.DRAFT, "Only draft requests can be edited.");
        AppUser assignee = resolveAssignee(request.assignedToId(), RoleName.MANAGER);
        workflow.updateDraft(request.title().trim(), clean(request.description()), assignee);
        audit(workflow, getActor(principal), WorkflowAction.UPDATE, WorkflowStatus.DRAFT, WorkflowStatus.DRAFT, "Draft details updated.");
        return toDetail(workflow, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse submit(Long requestId, WorkflowDtos.WorkflowActionRequest actionRequest, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        ensureOwner(workflow, principal);
        ensureStatus(workflow, WorkflowStatus.DRAFT, "Only draft requests can be submitted.");
        AppUser actor = getActor(principal);
        if (workflow.getAssignedTo() == null) {
            workflow.setAssignedTo(resolveAssignee(null, RoleName.MANAGER));
        }
        transition(workflow, actor, WorkflowAction.SUBMIT, WorkflowStatus.SUBMITTED, commentsOrDefault(actionRequest, "Submitted for manager approval."));
        return toDetail(workflow, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse approve(Long requestId, WorkflowDtos.WorkflowActionRequest actionRequest, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        AppUser actor = getActor(principal);

        if (workflow.getStatus() == WorkflowStatus.SUBMITTED) {
            ensureRole(principal, RoleName.MANAGER, "Only managers can approve submitted requests.");
            workflow.setAssignedTo(resolveAssignee(null, RoleName.HR_ADMIN));
            transition(workflow, actor, WorkflowAction.MANAGER_APPROVE, WorkflowStatus.MANAGER_APPROVED, commentsOrDefault(actionRequest, "Manager approved."));
            return toDetail(workflow, principal);
        }

        if (workflow.getStatus() == WorkflowStatus.MANAGER_APPROVED) {
            ensureRole(principal, RoleName.HR_ADMIN, "Only HR admins can approve after manager approval.");
            workflow.setAssignedTo(actor);
            transition(workflow, actor, WorkflowAction.HR_APPROVE, WorkflowStatus.HR_APPROVED, commentsOrDefault(actionRequest, "HR approved."));
            return toDetail(workflow, principal);
        }

        throw new ApiException(HttpStatus.BAD_REQUEST, "This request is not waiting for approval.");
    }

    public WorkflowDtos.WorkflowDetailResponse reject(Long requestId, WorkflowDtos.WorkflowActionRequest actionRequest, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        if (workflow.getStatus() == WorkflowStatus.SUBMITTED) {
            ensureRole(principal, RoleName.MANAGER, "Only managers can reject submitted requests.");
        } else if (workflow.getStatus() == WorkflowStatus.MANAGER_APPROVED || workflow.getStatus() == WorkflowStatus.HR_APPROVED) {
            ensureRole(principal, RoleName.HR_ADMIN, "Only HR admins can reject after manager approval.");
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This request cannot be rejected from its current status.");
        }

        transition(workflow, getActor(principal), WorkflowAction.REJECT, WorkflowStatus.REJECTED, commentsOrDefault(actionRequest, "Rejected."));
        return toDetail(workflow, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse complete(Long requestId, WorkflowDtos.WorkflowActionRequest actionRequest, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        ensureRole(principal, RoleName.HR_ADMIN, "Only HR admins can complete workflows.");
        ensureStatus(workflow, WorkflowStatus.HR_APPROVED, "Only HR-approved requests can be completed.");
        transition(workflow, getActor(principal), WorkflowAction.COMPLETE, WorkflowStatus.COMPLETED, commentsOrDefault(actionRequest, "Workflow completed."));
        return toDetail(workflow, principal);
    }

    public WorkflowDtos.WorkflowDetailResponse addComment(Long requestId, WorkflowDtos.WorkflowActionRequest actionRequest, UserPrincipal principal) {
        WorkflowRequest workflow = getVisibleRequest(requestId, principal);
        if (workflow.getStatus().isTerminal()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comments cannot be added to terminal workflows.");
        }
        if (!principal.hasRole(RoleName.MANAGER) && !principal.hasRole(RoleName.HR_ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only managers and HR admins can add workflow comments.");
        }
        String comments = actionRequest == null ? null : actionRequest.comments();
        if (!StringUtils.hasText(comments)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comment text is required.");
        }
        audit(workflow, getActor(principal), WorkflowAction.COMMENT, workflow.getStatus(), workflow.getStatus(), comments.trim());
        return toDetail(workflow, principal);
    }

    private WorkflowRequest getVisibleRequest(Long requestId, UserPrincipal principal) {
        WorkflowRequest request = workflowRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Workflow request not found."));
        if (!principal.hasRole(RoleName.MANAGER) && !principal.hasRole(RoleName.HR_ADMIN) && !isOwner(request, principal)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this workflow request.");
        }
        return request;
    }

    private void transition(WorkflowRequest workflow, AppUser actor, WorkflowAction action, WorkflowStatus newStatus, String comments) {
        WorkflowStatus previous = workflow.getStatus();
        workflow.transitionTo(newStatus);
        audit(workflow, actor, action, previous, newStatus, comments);
    }

    private void audit(
            WorkflowRequest workflow,
            AppUser actor,
            WorkflowAction action,
            WorkflowStatus previousStatus,
            WorkflowStatus newStatus,
            String comments
    ) {
        auditRepository.save(new WorkflowAudit(workflow, actor, action, previousStatus, newStatus, clean(comments)));
    }

    private WorkflowDtos.WorkflowSummaryResponse toSummary(WorkflowRequest request) {
        return new WorkflowDtos.WorkflowSummaryResponse(
                request.getId(),
                request.getTitle(),
                request.getStatus(),
                request.getStatus().label(),
                userService.toResponse(request.getCreatedBy()),
                userService.toResponse(request.getAssignedTo()),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    private WorkflowDtos.WorkflowDetailResponse toDetail(WorkflowRequest request, UserPrincipal principal) {
        List<WorkflowDtos.AuditResponse> audits = auditRepository.findByRequestIdOrderByCreatedAtAsc(request.getId())
                .stream()
                .map(audit -> new WorkflowDtos.AuditResponse(
                        audit.getId(),
                        audit.getCreatedAt(),
                        userService.toResponse(audit.getUser()),
                        audit.getAction(),
                        audit.getPreviousStatus(),
                        audit.getNewStatus(),
                        audit.getComments()
                ))
                .toList();

        return new WorkflowDtos.WorkflowDetailResponse(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getStatus().label(),
                userService.toResponse(request.getCreatedBy()),
                userService.toResponse(request.getAssignedTo()),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                availableActions(request, principal),
                audits
        );
    }

    private List<String> availableActions(WorkflowRequest request, UserPrincipal principal) {
        List<String> actions = new ArrayList<>();
        if (request.getStatus() == WorkflowStatus.DRAFT && isOwner(request, principal)) {
            actions.add("EDIT_DRAFT");
            actions.add("SUBMIT");
        }
        if (request.getStatus() == WorkflowStatus.SUBMITTED && principal.hasRole(RoleName.MANAGER)) {
            actions.add("APPROVE");
            actions.add("REJECT");
        }
        if (request.getStatus() == WorkflowStatus.MANAGER_APPROVED && principal.hasRole(RoleName.HR_ADMIN)) {
            actions.add("APPROVE");
            actions.add("REJECT");
        }
        if (request.getStatus() == WorkflowStatus.HR_APPROVED && principal.hasRole(RoleName.HR_ADMIN)) {
            actions.add("COMPLETE");
            actions.add("REJECT");
        }
        if (!request.getStatus().isTerminal() && (principal.hasRole(RoleName.MANAGER) || principal.hasRole(RoleName.HR_ADMIN))) {
            actions.add("COMMENT");
        }
        return actions;
    }

    private AppUser resolveAssignee(Long userId, RoleName fallbackRole) {
        if (userId != null) {
            AppUser assignee = userService.getUserOrThrow(userId);
            if (!assignee.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned user must be active.");
            }
            return assignee;
        }
        return userService.findFirstActiveByRole(fallbackRole).orElse(null);
    }

    private AppUser getActor(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private void ensureOwner(WorkflowRequest request, UserPrincipal principal) {
        if (!isOwner(request, principal)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the request creator can perform this action.");
        }
    }

    private void ensureRole(UserPrincipal principal, RoleName roleName, String message) {
        if (!principal.hasRole(roleName)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void ensureStatus(WorkflowRequest request, WorkflowStatus expectedStatus, String message) {
        if (request.getStatus() != expectedStatus) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private boolean isOwner(WorkflowRequest request, UserPrincipal principal) {
        return request.getCreatedBy().getId().equals(principal.getId());
    }

    private String safeSort(String sort) {
        if (sort == null) {
            return "updatedAt";
        }
        return switch (sort) {
            case "id", "title", "status", "createdAt", "updatedAt" -> sort;
            case "createdBy" -> "createdBy.fullName";
            case "assignedTo" -> "assignedTo.fullName";
            default -> "updatedAt";
        };
    }

    private String commentsOrDefault(WorkflowDtos.WorkflowActionRequest actionRequest, String fallback) {
        if (actionRequest != null && StringUtils.hasText(actionRequest.comments())) {
            return actionRequest.comments().trim();
        }
        return fallback;
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
