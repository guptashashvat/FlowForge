package com.flowforge.application;

import com.flowforge.repository.AppUserRepository;
import com.flowforge.security.JwtService;
import com.flowforge.security.UserPrincipal;
import com.flowforge.web.dto.AuthDtos;
import com.flowforge.web.dto.UserDtos;
import com.flowforge.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepository;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AppUserRepository userRepository,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return new AuthDtos.AuthResponse(jwtService.generateToken(principal), "Bearer", currentUser(principal));
    }

    @Transactional(readOnly = true)
    public UserDtos.UserResponse currentUser(UserPrincipal principal) {
        return userRepository.findByEmailIgnoreCase(principal.getEmail())
                .map(userService::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
