package com.flowforge.web.dto;

import com.flowforge.domain.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(
            Long id,
            String email,
            String fullName,
            boolean active,
            Set<RoleName> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Size(min = 8, max = 120) String password,
            @NotEmpty Set<RoleName> roles
    ) {
    }

    public record SetUserActiveRequest(boolean active) {
    }

    public record AssignRolesRequest(@NotEmpty Set<RoleName> roles) {
    }
}
