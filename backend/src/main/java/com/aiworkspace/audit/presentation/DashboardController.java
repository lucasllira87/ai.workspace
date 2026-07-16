package com.aiworkspace.audit.presentation;

import com.aiworkspace.audit.application.port.in.GetDashboardStatsUseCase;
import com.aiworkspace.audit.application.port.out.AuditUserContextPort;
import com.aiworkspace.audit.domain.model.DashboardStats;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final GetDashboardStatsUseCase getDashboardStats;
    private final AuditUserContextPort userContext;

    public DashboardController(GetDashboardStatsUseCase getDashboardStats,
                                AuditUserContextPort userContext) {
        this.getDashboardStats = getDashboardStats;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<DashboardStats> get() {
        UUID userId = userContext.currentUserId();
        return ResponseEntity.ok(getDashboardStats.getFor(userId));
    }
}
