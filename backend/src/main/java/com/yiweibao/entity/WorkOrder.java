package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false, length = 50)
    private String reporter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String faultDesc;

    @Column(length = 20)
    private String faultCategory;

    @Column(nullable = false)
    private Integer urgency; // 0-普通 1-紧急 2-特急

    @Column(columnDefinition = "TEXT")
    private String photos; // JSON array of photo paths

    @Column(nullable = false)
    private Integer status; // 0-待处理 1-处理中 2-已完成 3-已撤销

    @Column(length = 50)
    private String repairEngineer;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String repairAction;

    @Column(columnDefinition = "TEXT")
    private String replacedParts;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public WorkOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public String getFaultDesc() { return faultDesc; }
    public void setFaultDesc(String faultDesc) { this.faultDesc = faultDesc; }
    public String getFaultCategory() { return faultCategory; }
    public void setFaultCategory(String faultCategory) { this.faultCategory = faultCategory; }
    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRepairEngineer() { return repairEngineer; }
    public void setRepairEngineer(String repairEngineer) { this.repairEngineer = repairEngineer; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getRepairAction() { return repairAction; }
    public void setRepairAction(String repairAction) { this.repairAction = repairAction; }
    public String getReplacedParts() { return replacedParts; }
    public void setReplacedParts(String replacedParts) { this.replacedParts = replacedParts; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
