package com.flowforge.application;

import com.flowforge.domain.entity.AppUser;
import com.flowforge.domain.entity.Role;
import com.flowforge.domain.model.RoleName;
import com.flowforge.repository.AppUserRepository;
import com.flowforge.repository.RoleRepository;
import com.flowforge.web.dto.UserDtos;
import com.flowforge.web.error.ApiException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> listUsers() {
        return userRepository.findAllByOrderByFullNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserDtos.UserResponse createUser(UserDtos.CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "A user with this email already exists.");
        }

        AppUser user = new AppUser(
                email,
                request.fullName().trim(),
                passwordEncoder.encode(request.password()),
                resolveRoles(request.roles())
        );
        return toResponse(userRepository.save(user));
    }

    public UserDtos.UserResponse setActive(Long userId, boolean active) {
        AppUser user = getUserOrThrow(userId);
        user.setActive(active);
        return toResponse(user);
    }

    public UserDtos.UserResponse assignRoles(Long userId, Set<RoleName> roles) {
        AppUser user = getUserOrThrow(userId);
        user.setRoles(resolveRoles(roles));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findFirstActiveByRole(RoleName roleName) {
        return userRepository.findByRoles_NameAndActiveTrueOrderByFullNameAsc(roleName).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public AppUser getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    public UserDtos.UserResponse toResponse(AppUser user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoleNames(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown role: " + roleName)))
                .collect(Collectors.toSet());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
