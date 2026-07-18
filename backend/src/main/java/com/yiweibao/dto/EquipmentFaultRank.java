package com.yiweibao.dto;

public class EquipmentFaultRank {
    private Long equipmentId;
    private String equipmentName;
    private String workshop;
    private long faultCount;

    public EquipmentFaultRank(Long equipmentId, String equipmentName, String workshop, long faultCount) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.workshop = workshop;
        this.faultCount = faultCount;
    }

    public Long getEquipmentId() { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public String getWorkshop() { return workshop; }
    public long getFaultCount() { return faultCount; }
}
