package com.yiweibao.dto;

import java.time.LocalDateTime;

public class DiagnosisCaseVO {
    private Long id;
    private Long workOrderId;
    private String orderNo;
    private Long equipmentId;
    private String equipmentName;
    private String faultCategory;
    private String faultDesc;
    private String diagnosis;
    private String repairAction;
    private String replacedParts;
    private String repairEngineer;
    private Integer status;
    private LocalDateTime createdAt;

    public DiagnosisCaseVO() {}

    public static DiagnosisCaseVO from(com.yiweibao.entity.DiagnosisCase c) {
        DiagnosisCaseVO vo = new DiagnosisCaseVO();
        vo.id = c.getId();
        vo.workOrderId = c.getWorkOrder().getId();
        vo.orderNo = c.getWorkOrder().getOrderNo();
        vo.equipmentId = c.getEquipment().getId();
        vo.equipmentName = c.getEquipmentName();
        vo.faultCategory = c.getFaultCategory();
        vo.faultDesc = c.getFaultDesc();
        vo.diagnosis = c.getDiagnosis();
        vo.repairAction = c.getRepairAction();
        vo.replacedParts = c.getReplacedParts();
        vo.repairEngineer = c.getRepairEngineer();
        vo.status = c.getStatus();
        vo.createdAt = c.getCreatedAt();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long id) { this.workOrderId = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String s) { this.orderNo = s; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long id) { this.equipmentId = id; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String s) { this.equipmentName = s; }
    public String getFaultCategory() { return faultCategory; }
    public void setFaultCategory(String s) { this.faultCategory = s; }
    public String getFaultDesc() { return faultDesc; }
    public void setFaultDesc(String s) { this.faultDesc = s; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String s) { this.diagnosis = s; }
    public String getRepairAction() { return repairAction; }
    public void setRepairAction(String s) { this.repairAction = s; }
    public String getReplacedParts() { return replacedParts; }
    public void setReplacedParts(String s) { this.replacedParts = s; }
    public String getRepairEngineer() { return repairEngineer; }
    public void setRepairEngineer(String s) { this.repairEngineer = s; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer n) { this.status = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}
