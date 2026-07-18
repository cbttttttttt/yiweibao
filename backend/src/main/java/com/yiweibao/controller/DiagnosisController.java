package com.yiweibao.controller;

import com.yiweibao.dto.ApiResponse;
import com.yiweibao.dto.DiagnosisCaseVO;
import com.yiweibao.dto.DiagnosisVO;
import com.yiweibao.service.DiagnosisService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping("/equipment/{equipmentId}")
    public ApiResponse<List<DiagnosisVO>> diagnose(@PathVariable Long equipmentId) {
        return ApiResponse.success(diagnosisService.diagnose(equipmentId));
    }

    @GetMapping("/cases")
    public ApiResponse<Page<DiagnosisCaseVO>> listCases(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(diagnosisService.listCases(status, page, size));
    }

    @GetMapping("/cases/equipment/{equipmentId}")
    public ApiResponse<List<DiagnosisCaseVO>> getCasesByEquipment(@PathVariable Long equipmentId) {
        return ApiResponse.success(diagnosisService.getCasesByEquipment(equipmentId));
    }

    @PutMapping("/cases/{id}/adopt")
    public ApiResponse<DiagnosisCaseVO> adoptCase(@PathVariable Long id) {
        var c = diagnosisService.adoptCase(id);
        return ApiResponse.success(DiagnosisCaseVO.from(c));
    }

    @PutMapping("/cases/{id}/ignore")
    public ApiResponse<DiagnosisCaseVO> ignoreCase(@PathVariable Long id) {
        var c = diagnosisService.ignoreCase(id);
        return ApiResponse.success(DiagnosisCaseVO.from(c));
    }

    @PutMapping("/cases/{id}/promote-to-rule")
    public ApiResponse<Map<String, Object>> promoteToRule(@PathVariable Long id) {
        var result = diagnosisService.promoteToRule(id);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("rule", DiagnosisVO.from(result.getRule()));
        response.put("merged", result.isMerged());
        response.put("ruleId", result.getRule().getId());
        return ApiResponse.success(response);
    }

    @GetMapping("/rules")
    public ApiResponse<List<DiagnosisVO>> listRules() {
        List<DiagnosisVO> rules = diagnosisService.listActiveRules()
                .stream().map(DiagnosisVO::from).toList();
        return ApiResponse.success(rules);
    }
}
