package ro.utcn.sportbuddy.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.dashboard.dto.DashboardDto;
import ro.utcn.sportbuddy.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/home")
    public DashboardDto home(@RequestParam Long cityId) {
        return dashboardService.getHome(CurrentUser.id(), cityId);
    }
}
