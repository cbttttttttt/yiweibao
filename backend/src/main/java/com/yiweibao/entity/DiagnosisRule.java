package com.yiweibao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_rules")
public class DiagnosisRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String symptomDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conditionJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String possibleCause;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false, length = 50)
    private String faultCategory;

    @Column(nullable = false)
    private Integer severityLevel;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Long matchCount = 0L;

    @Column(nullable = false)
    private Integer verifiedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String alternativeActions; // JSON array of {diagnosis,action,engineer,date}

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public DiagnosisRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSymptomDescription() { return symptomDescription; }
    public void setSymptomDescription(String s) { this.symptomDescription = s; }
    public String getConditionJson() { return conditionJson; }
    public void setConditionJson(String s) { this.conditionJson = s; }
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
    public Boolean getActive() { return active; }
    public void setActive(Boolean b) { this.active = b; }
    public Long getMatchCount() { return matchCount; }
    public void setMatchCount(Long n) { this.matchCount = n; }
    public Integer getVerifiedCount() { return verifiedCount; }
    public void setVerifiedCount(Integer n) { this.verifiedCount = n; }
    public String getAlternativeActions() { return alternativeActions; }
    public void setAlternativeActions(String s) { this.alternativeActions = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}
