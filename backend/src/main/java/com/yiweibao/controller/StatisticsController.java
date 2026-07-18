package com.yiweibao.controller;

import com.yiweibao.dto.*;
import com.yiweibao.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<StatisticsOverview> overview() {
        return ApiResponse.success(statisticsService.getOverview());
    }

    @GetMapping("/fault-types")
    public ApiResponse<List<FaultTypeStat>> faultTypes() {
        return ApiResponse.success(statisticsService.getFaultTypes());
    }

    @GetMapping("/fault-avg-time")
    public ApiResponse<List<FaultTypeAvgTime>> faultAvgTime() {
        return ApiResponse.success(statisticsService.getFaultAvgTime());
    }

    @GetMapping("/top-equipment")
    public ApiResponse<List<EquipmentFaultRank>> topEquipment(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(statisticsService.getTopEquipment(limit));
    }
}
