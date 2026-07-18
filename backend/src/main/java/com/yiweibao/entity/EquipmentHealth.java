package com.yiweibao.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_health")
public class EquipmentHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false, unique = true)
    private Long equipmentId;

    @Column(nullable = false)
    private Double bearingWear = 0.0;   // 轴承磨损 0~1

    @Column(nullable = false)
    private Double coolingDecay = 0.0;  // 冷却系统衰减 0~1

    @Column(nullable = false)
    private Double toolWear = 0.0;      // 刀具磨损 0~1

    public EquipmentHealth() {}

    public EquipmentHealth(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long v) { this.equipmentId = v; }
    public Double getBearingWear() { return bearingWear; }
    public void setBearingWear(Double v) { this.bearingWear = clamp(v); }
    public Double getCoolingDecay() { return coolingDecay; }
    public void setCoolingDecay(Double v) { this.coolingDecay = clamp(v); }
    public Double getToolWear() { return toolWear; }
    public void setToolWear(Double v) { this.toolWear = clamp(v); }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
