package com.flowforge.domain.entity;

import com.flowforge.domain.model.WorkflowAction;
import com.flowforge.domain.model.WorkflowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflow_audits")
public class WorkflowAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_request_id")
    private WorkflowRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private WorkflowAction action;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private WorkflowStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WorkflowStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected WorkflowAudit() {
    }

    public WorkflowAudit(
            WorkflowRequest request,
            AppUser user,
            WorkflowAction action,
            WorkflowStatus previousStatus,
            WorkflowStatus newStatus,
            String comments
    ) {
        this.request = request;
        this.user = user;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comments = comments;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public WorkflowRequest getRequest() {
        return request;
    }

    public AppUser getUser() {
        return user;
    }

    public WorkflowAction getAction() {
        return action;
    }

    public WorkflowStatus getPreviousStatus() {
        return previousStatus;
    }

    public WorkflowStatus getNewStatus() {
        return newStatus;
    }

    public String getComments() {
        return comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
