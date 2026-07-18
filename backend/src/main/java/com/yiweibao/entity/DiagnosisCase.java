package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_cases")
public class DiagnosisCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(length = 100)
    private String equipmentName;

    @Column(nullable = false, length = 50)
    private String faultCategory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String faultDesc;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String repairAction;

    @Column(columnDefinition = "TEXT")
    private String replacedParts;

    @Column(length = 50)
    private String repairEngineer;

    @Column(columnDefinition = "TEXT")
    private String sensorSnapshot;

    @Column(nullable = false)
    private Integer status; // 0=待审核 1=已采纳 2=已忽略

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public DiagnosisCase() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder wo) { this.workOrder = wo; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment e) { this.equipment = e; }
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
    public String getSensorSnapshot() { return sensorSnapshot; }
    public void setSensorSnapshot(String s) { this.sensorSnapshot = s; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer n) { this.status = n; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}
