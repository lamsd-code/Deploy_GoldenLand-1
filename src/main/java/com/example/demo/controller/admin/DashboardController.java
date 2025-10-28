package com.example.demo.controller.admin;


import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin/dashboard-data")
    public Map<String, Object> getDashboardData() {
        return dashboardService.getDashboardData();
    }
}


