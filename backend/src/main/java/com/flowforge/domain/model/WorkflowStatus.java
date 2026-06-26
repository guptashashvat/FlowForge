package com.flowforge.domain.model;

public enum WorkflowStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    MANAGER_APPROVED("Manager Approved"),
    HR_APPROVED("HR Approved"),
    COMPLETED("Completed"),
    REJECTED("Rejected");

    private final String label;

    WorkflowStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED;
    }
}
