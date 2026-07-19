package com.yiweibao.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiweibao.dto.DiagnosisCaseVO;
import com.yiweibao.dto.DiagnosisVO;
import com.yiweibao.entity.*;
import com.yiweibao.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisService.class);
    private final DiagnosisRuleRepository ruleRepo;
    private final DiagnosisCaseRepository caseRepo;
    private final MachineDataRepository machineDataRepo;
    private final ObjectMapper objectMapper;

    public DiagnosisService(DiagnosisRuleRepository ruleRepo, DiagnosisCaseRepository caseRepo,
                            MachineDataRepository machineDataRepo, ObjectMapper objectMapper) {
        this.ruleRepo = ruleRepo;
        this.caseRepo = caseRepo;
        this.machineDataRepo = machineDataRepo;
        this.objectMapper = objectMapper;
    }

    public List<DiagnosisVO> diagnose(Long equipmentId) {
        MachineData latest = machineDataRepo.findLatestByEquipment(equipmentId);
        if (latest == null || latest.getStatus() == 0) return Collections.emptyList();

        List<DiagnosisRule> rules = ruleRepo.findByActiveTrueOrderByPriorityAsc();
        List<DiagnosisVO> matched = new ArrayList<>();

        for (DiagnosisRule rule : rules) {
            try {
                if (evaluateConditions(latest, rule.getConditionJson())) {
                    rule.setMatchCount(rule.getMatchCount() + 1);
                    ruleRepo.save(rule);
                    matched.add(DiagnosisVO.from(rule));
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate rule {}: {}", rule.getId(), e.getMessage());
            }
        }

        matched.sort(Comparator
                .comparing(DiagnosisVO::getSeverityLevel).reversed()
                .thenComparing(DiagnosisVO::getPriority));
        return matched.size() > 5 ? matched.subList(0, 5) : matched;
    }

    private boolean evaluateConditions(MachineData data, String conditionJson) throws Exception {
        List<List<Map<String, Object>>> groups = objectMapper.readValue(
                conditionJson, new TypeReference<List<List<Map<String, Object>>>>() {});
        for (List<Map<String, Object>> andGroup : groups) {
            boolean allMatch = true;
            for (Map<String, Object> cond : andGroup) {
                String field = (String) cond.get("field");
                String operator = (String) cond.get("operator");
                double threshold = ((Number) cond.get("value")).doubleValue();
                double actual = getFieldValue(data, field);
                if (!evaluate(actual, operator, threshold)) { allMatch = false; break; }
            }
            if (allMatch) return true;
        }
        return false;
    }

    // For merge detection: ignore "status" field, check only sensor values
    private boolean evaluateConditionsFromMap(Map<String, Object> snap, String conditionJson) throws Exception {
        List<List<Map<String, Object>>> groups = objectMapper.readValue(
                conditionJson, new TypeReference<List<List<Map<String, Object>>>>() {});
        for (List<Map<String, Object>> andGroup : groups) {
            boolean allMatch = true;
            for (Map<String, Object> cond : andGroup) {
                String field = (String) cond.get("field");
                if ("status".equals(field)) continue; // skip status for merge detection
                String operator = (String) cond.get("operator");
                double threshold = ((Number) cond.get("value")).doubleValue();
                Object val = snap.get(field);
                if (val == null) { allMatch = false; break; }
                double actual = val instanceof Number ? ((Number) val).doubleValue() : 0;
                if (!evaluate(actual, operator, threshold)) { allMatch = false; break; }
            }
            if (allMatch) return true;
        }
        return false;
    }

    private double getFieldValue(MachineData data, String field) {
        return switch (field) {
            case "spindleSpeed" -> data.getSpindleSpeed();
            case "temperature" -> data.getTemperature();
            case "vibration" -> data.getVibration();
            case "current" -> data.getCurrent();
            case "power" -> data.getPower();
            case "pressure" -> data.getPressure();
            case "status" -> data.getStatus().doubleValue();
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }

    private boolean evaluate(double actual, String operator, double threshold) {
        return switch (operator) {
            case ">" -> actual > threshold;
            case ">=" -> actual >= threshold;
            case "<" -> actual < threshold;
            case "<=" -> actual <= threshold;
            case "==" -> Math.abs(actual - threshold) < 0.001;
            case "!=" -> Math.abs(actual - threshold) >= 0.001;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }

    @Transactional
    public DiagnosisCase createCaseFromWorkOrder(WorkOrder wo) {
        DiagnosisCase dc = new DiagnosisCase();
        dc.setWorkOrder(wo);
        dc.setEquipment(wo.getEquipment());
        dc.setEquipmentName(wo.getEquipment().getName());
        dc.setFaultCategory(wo.getFaultCategory());
        dc.setFaultDesc(wo.getFaultDesc());
        dc.setDiagnosis(wo.getDiagnosis());
        dc.setRepairAction(wo.getRepairAction());
        dc.setReplacedParts(wo.getReplacedParts());
        dc.setRepairEngineer(wo.getRepairEngineer());
        dc.setStatus(0);
        try {
            MachineData latest = machineDataRepo.findLatestByEquipment(wo.getEquipment().getId());
            if (latest != null) {
                Map<String, Object> snap = new LinkedHashMap<>();
                snap.put("spindleSpeed", latest.getSpindleSpeed());
                snap.put("temperature", latest.getTemperature());
                snap.put("vibration", latest.getVibration());
                snap.put("current", latest.getCurrent());
                snap.put("power", latest.getPower());
                snap.put("pressure", latest.getPressure());
                snap.put("machineStatus", latest.getStatus());
                dc.setSensorSnapshot(objectMapper.writeValueAsString(snap));
            }
        } catch (Exception e) {
            log.warn("Failed to capture sensor snapshot for WO {}", wo.getId());
        }
        log.info("Diagnosis case created from WO{}", wo.getId());
        return caseRepo.save(dc);
    }

    @Transactional
    public DiagnosisCase adoptCase(Long caseId) {
        DiagnosisCase dc = caseRepo.findById(caseId)
                .orElseThrow(() -> new RuntimeException("案例不存在"));
        dc.setStatus(1);
        List<DiagnosisRule> rules = ruleRepo.findByActiveTrueOrderByPriorityAsc();
        for (DiagnosisRule rule : rules) {
            if (rule.getFaultCategory().equals(dc.getFaultCategory())) {
                try {
                    MachineData latest = machineDataRepo.findLatestByEquipment(dc.getEquipment().getId());
                    if (latest != null && evaluateConditions(latest, rule.getConditionJson())) {
                        rule.setVerifiedCount(rule.getVerifiedCount() + 1);
                        ruleRepo.save(rule);
                    }
                } catch (Exception ignored) {}
            }
        }
        return caseRepo.save(dc);
    }

    @Transactional
    public DiagnosisCase ignoreCase(Long caseId) {
        DiagnosisCase dc = caseRepo.findById(caseId)
                .orElseThrow(() -> new RuntimeException("案例不存在"));
        dc.setStatus(2);
        return caseRepo.save(dc);
    }

    public Page<DiagnosisCaseVO> listCases(Integer status, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<DiagnosisCase> cases = status != null ? caseRepo.findByStatus(status, pr) : caseRepo.findAll(pr);
        return cases.map(DiagnosisCaseVO::from);
    }

    public List<DiagnosisRule> listActiveRules() {
        return ruleRepo.findByActiveTrueOrderByPriorityAsc();
    }

    public List<DiagnosisRule> listRulesByCategory(String faultCategory) {
        return ruleRepo.findByFaultCategoryAndActiveTrueOrderByPriorityAsc(faultCategory);
    }

    public List<DiagnosisRule> searchRules(String keyword, String faultCategory) {
        if (keyword == null || keyword.isBlank()) {
            return faultCategory != null && !faultCategory.isBlank()
                    ? listRulesByCategory(faultCategory) : listActiveRules();
        }
        return faultCategory != null && !faultCategory.isBlank()
                ? ruleRepo.searchByCategoryAndKeyword(faultCategory, keyword)
                : ruleRepo.searchByKeyword(keyword);
    }

    public List<DiagnosisCaseVO> searchCases(String keyword, String faultCategory) {
        List<DiagnosisCase> cases;
        if (keyword == null || keyword.isBlank()) {
            cases = caseRepo.findByEquipmentIdOrderByCreatedAtDesc(null);
            if (faultCategory != null && !faultCategory.isBlank()) {
                cases = cases.stream()
                        .filter(c -> faultCategory.equals(c.getFaultCategory()))
                        .toList();
            }
            return cases.stream().map(DiagnosisCaseVO::from).toList();
        }
        cases = faultCategory != null && !faultCategory.isBlank()
                ? caseRepo.searchByCategoryAndKeyword(faultCategory, keyword)
                : caseRepo.searchByKeyword(keyword);
        return cases.stream().map(DiagnosisCaseVO::from).toList();
    }

    public List<DiagnosisCaseVO> getCasesByEquipment(Long equipmentId) {
        return caseRepo.findByEquipmentIdOrderByCreatedAtDesc(equipmentId)
                .stream().map(DiagnosisCaseVO::from).toList();
    }

    @Transactional
    public PromoteResult promoteToRule(Long caseId) {
        DiagnosisCase dc = caseRepo.findById(caseId)
                .orElseThrow(() -> new RuntimeException("案例不存在"));

        // Check if a similar rule already exists
        List<DiagnosisRule> existingRules = ruleRepo.findByActiveTrueOrderByPriorityAsc();
        for (DiagnosisRule existing : existingRules) {
            if (existing.getFaultCategory().equals(dc.getFaultCategory())) {
                try {
                    if (dc.getSensorSnapshot() != null) {
                        Map<String, Object> snap = objectMapper.readValue(
                                dc.getSensorSnapshot(), new TypeReference<Map<String, Object>>() {});
                        if (evaluateConditionsFromMap(snap, existing.getConditionJson())) {
                            appendAlternative(existing, dc);
                            dc.setStatus(1);
                            caseRepo.save(dc);
                            existing.setVerifiedCount(existing.getVerifiedCount() + 1);
                            log.info("Merged case {} into existing rule {} ({})", caseId, existing.getId(), existing.getName());
                            return new PromoteResult(ruleRepo.save(existing), true);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // No similar rule: create new one
        DiagnosisRule rule = new DiagnosisRule();
        String ruleName = dc.getFaultCategory() + "-" + dc.getEquipmentName();
        rule.setName(ruleName);
        rule.setSymptomDescription(dc.getFaultDesc());
        rule.setPossibleCause(dc.getDiagnosis());
        rule.setRecommendedAction(dc.getRepairAction());
        rule.setFaultCategory(dc.getFaultCategory());
        rule.setSeverityLevel(1);
        rule.setPriority(50);
        rule.setActive(true);
        rule.setVerifiedCount(1);

        if (dc.getSensorSnapshot() != null) {
            try {
                Map<String, Object> snap = objectMapper.readValue(
                        dc.getSensorSnapshot(), new TypeReference<Map<String, Object>>() {});
                List<List<Map<String, Object>>> conditions = new ArrayList<>();
                List<Map<String, Object>> andGroup = new ArrayList<>();

                addConditionIf(snap, andGroup, "temperature", ">", 60.0);
                addConditionIf(snap, andGroup, "vibration", ">", 3.5);
                addConditionIf(snap, andGroup, "current", ">", 22.0);
                addConditionIf(snap, andGroup, "power", ">", 10.0);

                if (!andGroup.isEmpty()) {
                    conditions.add(andGroup);
                    rule.setConditionJson(objectMapper.writeValueAsString(conditions));
                } else {
                    rule.setConditionJson("[[{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]");
                }
            } catch (Exception e) {
                rule.setConditionJson("[[{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]");
            }
        } else {
            rule.setConditionJson("[[{\"field\":\"status\",\"operator\":\">=\",\"value\":1}]]");
        }

        dc.setStatus(1);
        caseRepo.save(dc);
        log.info("Promoted diagnosis case {} to new rule: {}", caseId, ruleName);
        return new PromoteResult(ruleRepo.save(rule), false);
    }

    private void appendAlternative(DiagnosisRule rule, DiagnosisCase dc) {
        List<Map<String, String>> alternatives;
        try {
            if (rule.getAlternativeActions() != null && !rule.getAlternativeActions().isBlank()) {
                alternatives = objectMapper.readValue(rule.getAlternativeActions(),
                        new TypeReference<List<Map<String, String>>>() {});
            } else {
                alternatives = new ArrayList<>();
            }
        } catch (Exception e) {
            alternatives = new ArrayList<>();
        }

        Map<String, String> alt = new LinkedHashMap<>();
        alt.put("diagnosis", dc.getDiagnosis());
        alt.put("action", dc.getRepairAction());
        alt.put("engineer", dc.getRepairEngineer() != null ? dc.getRepairEngineer() : "未知");
        alt.put("date", dc.getCreatedAt().toString());
        alternatives.add(alt);

        try {
            rule.setAlternativeActions(objectMapper.writeValueAsString(alternatives));
        } catch (Exception ignored) {}
    }

    // Result class for promoteToRule
    public static class PromoteResult {
        private final DiagnosisRule rule;
        private final boolean merged;

        public PromoteResult(DiagnosisRule rule, boolean merged) {
            this.rule = rule;
            this.merged = merged;
        }
        public DiagnosisRule getRule() { return rule; }
        public boolean isMerged() { return merged; }
    }

    private void addConditionIf(Map<String, Object> snap, List<Map<String, Object>> group,
                                 String field, String operator, double threshold) {
        Object val = snap.get(field);
        if (val instanceof Number) {
            double v = ((Number) val).doubleValue();
            if (v > threshold) {
                Map<String, Object> cond = new LinkedHashMap<>();
                cond.put("field", field);
                cond.put("operator", operator);
                cond.put("value", threshold);
                group.add(cond);
            }
        }
    }
}
