package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.dto.MachineDataVO;
import com.yiweibao.service.MachineDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machine-data")
public class MachineDataController {

    private final MachineDataService machineDataService;

    public MachineDataController(MachineDataService machineDataService) {
        this.machineDataService = machineDataService;
    }

    @GetMapping("/realtime")
    public ApiResponse<List<MachineDataVO>> getRealtimeAll() {
        List<MachineDataVO> data = machineDataService.getRealtimeAll();
        return ApiResponse.success(data);
    }

    @GetMapping("/equipment/{id}/realtime")
    public ApiResponse<List<MachineDataVO>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int minutes) {
        List<MachineDataVO> data = machineDataService.getHistoryByEquipment(id, Math.min(minutes, 120));
        return ApiResponse.success(data);
    }

    @GetMapping("/equipment/{id}/latest")
    public ApiResponse<MachineDataVO> getLatest(@PathVariable Long id) {
        MachineDataVO data = machineDataService.getLatestByEquipment(id);
        return ApiResponse.success(data);
    }
}
