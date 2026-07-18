package com.yiweibao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WorkOrderCompleteRequest {
    @NotBlank @Size(min = 10, message = "维修描述不少于10字")
    private String diagnosis;

    @NotBlank @Size(min = 20, message = "维修记录不少于20字")
    private String repairAction;

    private String replacedParts;
    private String photos;

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getRepairAction() { return repairAction; }
    public void setRepairAction(String repairAction) { this.repairAction = repairAction; }
    public String getReplacedParts() { return replacedParts; }
    public void setReplacedParts(String replacedParts) { this.replacedParts = replacedParts; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
}
