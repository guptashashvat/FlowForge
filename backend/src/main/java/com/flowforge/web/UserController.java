package com.flowforge.web;

import com.flowforge.application.UserService;
import com.flowforge.web.dto.UserDtos;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('HR_ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDtos.UserResponse> listUsers() {
        return userService.listUsers();
    }

    @PostMapping
    public UserDtos.UserResponse createUser(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PatchMapping("/{userId}/status")
    public UserDtos.UserResponse setActive(
            @PathVariable Long userId,
            @RequestBody UserDtos.SetUserActiveRequest request
    ) {
        return userService.setActive(userId, request.active());
    }

    @PatchMapping("/{userId}/roles")
    public UserDtos.UserResponse assignRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtos.AssignRolesRequest request
    ) {
        return userService.assignRoles(userId, request.roles());
    }
}
