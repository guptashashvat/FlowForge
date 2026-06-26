package com.flowforge.domain.model;

public enum RoleName {
    EMPLOYEE("Employee"),
    MANAGER("Manager"),
    HR_ADMIN("HR Admin");

    private final String label;

    RoleName(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
