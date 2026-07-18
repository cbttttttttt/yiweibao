package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.dto.EquipmentRequest;
import com.yiweibao.entity.Equipment;
import com.yiweibao.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ApiResponse<Page<Equipment>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(equipmentService.list(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Equipment> getById(@PathVariable Long id) {
        return ApiResponse.success(equipmentService.getById(id));
    }

    @PostMapping
    public ApiResponse<Equipment> create(@Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success(equipmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Equipment> update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success(equipmentService.update(id, request));
    }

    @PutMapping("/{id}/scrap")
    public ApiResponse<Equipment> scrap(@PathVariable Long id) {
        return ApiResponse.success(equipmentService.scrap(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ApiResponse.success(Map.of("message", "删除成功"));
    }
}
