package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_data", indexes = {
    @Index(name = "idx_machine_data_equip_time", columnList = "equipment_id,timestamp DESC")
})
public class MachineData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Double spindleSpeed;   // 主轴转速 (rpm)

    @Column(nullable = false)
    private Double temperature;    // 主轴温度 (°C)

    @Column(nullable = false)
    private Double vibration;      // 振动值 (mm/s)

    @Column(nullable = false)
    private Double current;        // 电流 (A)

    @Column(nullable = false)
    private Double power;          // 功率 (kW)

    @Column(nullable = false)
    private Double pressure;       // 液压压力 (MPa)

    @Column(nullable = false)
    private Integer status;        // 0-正常 1-预警 2-告警

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public MachineData() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public Double getSpindleSpeed() { return spindleSpeed; }
    public void setSpindleSpeed(Double spindleSpeed) { this.spindleSpeed = spindleSpeed; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getVibration() { return vibration; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    public Double getCurrent() { return current; }
    public void setCurrent(Double current) { this.current = current; }
    public Double getPower() { return power; }
    public void setPower(Double power) { this.power = power; }
    public Double getPressure() { return pressure; }
    public void setPressure(Double pressure) { this.pressure = pressure; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
