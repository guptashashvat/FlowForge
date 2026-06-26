package com.flowforge.web;

import com.flowforge.application.DashboardService;
import com.flowforge.security.UserPrincipal;
import com.flowforge.web.dto.DashboardDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDtos.DashboardResponse getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.getDashboard(principal);
    }
}
