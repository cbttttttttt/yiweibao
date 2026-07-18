package com.yiweibao.dto;

import java.time.LocalDateTime;

public class MachineDataVO {
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String workshop;
    private Double spindleSpeed;
    private Double temperature;
    private Double vibration;
    private Double current;
    private Double power;
    private Double pressure;
    private Integer status;
    private LocalDateTime timestamp;

    public MachineDataVO() {}

    public static MachineDataVO from(com.yiweibao.entity.MachineData md) {
        MachineDataVO vo = new MachineDataVO();
        vo.id = md.getId();
        vo.equipmentId = md.getEquipment().getId();
        vo.equipmentName = md.getEquipment().getName();
        vo.workshop = md.getEquipment().getWorkshop();
        vo.spindleSpeed = md.getSpindleSpeed();
        vo.temperature = md.getTemperature();
        vo.vibration = md.getVibration();
        vo.current = md.getCurrent();
        vo.power = md.getPower();
        vo.pressure = md.getPressure();
        vo.status = md.getStatus();
        vo.timestamp = md.getTimestamp();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
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
}
