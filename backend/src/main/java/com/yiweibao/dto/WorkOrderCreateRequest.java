package com.yiweibao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkOrderCreateRequest {
    @NotNull private Long equipmentId;
    @NotBlank private String faultDesc;
    @NotBlank private String faultCategory;
    private Integer urgency = 0;
    private String photos; // JSON array string
    @NotBlank private String reporter;

    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getFaultDesc() { return faultDesc; }
    public void setFaultDesc(String faultDesc) { this.faultDesc = faultDesc; }
    public String getFaultCategory() { return faultCategory; }
    public void setFaultCategory(String faultCategory) { this.faultCategory = faultCategory; }
    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }
}
