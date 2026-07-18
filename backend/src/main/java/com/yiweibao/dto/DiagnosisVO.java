package com.yiweibao.dto;

public class DiagnosisVO {
    private Long id;
    private String name;
    private String symptomDescription;
    private String possibleCause;
    private String recommendedAction;
    private String faultCategory;
    private Integer severityLevel;
    private Integer priority;
    private String alternativeActions;
    private Integer verifiedCount;

    public DiagnosisVO() {}

    public static DiagnosisVO from(com.yiweibao.entity.DiagnosisRule rule) {
        DiagnosisVO vo = new DiagnosisVO();
        vo.id = rule.getId();
        vo.name = rule.getName();
        vo.symptomDescription = rule.getSymptomDescription();
        vo.possibleCause = rule.getPossibleCause();
        vo.recommendedAction = rule.getRecommendedAction();
        vo.faultCategory = rule.getFaultCategory();
        vo.severityLevel = rule.getSeverityLevel();
        vo.priority = rule.getPriority();
        vo.verifiedCount = rule.getVerifiedCount();
        vo.alternativeActions = rule.getAlternativeActions();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String s) { this.name = s; }
    public String getSymptomDescription() { return symptomDescription; }
    public void setSymptomDescription(String s) { this.symptomDescription = s; }
    public String getPossibleCause() { return possibleCause; }
    public void setPossibleCause(String s) { this.possibleCause = s; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String s) { this.recommendedAction = s; }
    public String getFaultCategory() { return faultCategory; }
    public void setFaultCategory(String s) { this.faultCategory = s; }
    public Integer getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(Integer n) { this.severityLevel = n; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer n) { this.priority = n; }
    public Integer getVerifiedCount() { return verifiedCount; }
    public void setVerifiedCount(Integer n) { this.verifiedCount = n; }
    public String getAlternativeActions() { return alternativeActions; }
    public void setAlternativeActions(String s) { this.alternativeActions = s; }
}
