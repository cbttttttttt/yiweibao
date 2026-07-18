package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.service.HealthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/scores")
    public ApiResponse<List<HealthService.HealthScore>> getScores() {
        return ApiResponse.success(healthService.getHealthScores());
    }

    @GetMapping("/{equipmentId}")
    public ApiResponse<HealthService.HealthDetail> getDetail(@PathVariable Long equipmentId) {
        return ApiResponse.success(healthService.getHealthDetail(equipmentId));
    }
}
