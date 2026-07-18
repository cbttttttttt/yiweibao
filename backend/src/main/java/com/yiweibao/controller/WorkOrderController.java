package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.entity.User;
import com.yiweibao.entity.WorkOrder;
import com.yiweibao.dto.WorkOrderCompleteRequest;
import com.yiweibao.dto.WorkOrderCreateRequest;
import com.yiweibao.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public ApiResponse<Page<WorkOrder>> list(
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(workOrderService.list(status, equipmentId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrder> getById(@PathVariable Long id) {
        return ApiResponse.success(workOrderService.getById(id));
    }

    @PostMapping
    public ApiResponse<WorkOrder> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ApiResponse.success(workOrderService.create(request));
    }

    @PutMapping("/{id}/accept")
    public ApiResponse<WorkOrder> accept(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ApiResponse.success(workOrderService.accept(id, user.getRealName()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<WorkOrder> cancel(@PathVariable Long id) {
        return ApiResponse.success(workOrderService.cancel(id));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<WorkOrder> complete(@PathVariable Long id,
                                           @Valid @RequestBody WorkOrderCompleteRequest request) {
        return ApiResponse.success(workOrderService.complete(id, request));
    }
}
