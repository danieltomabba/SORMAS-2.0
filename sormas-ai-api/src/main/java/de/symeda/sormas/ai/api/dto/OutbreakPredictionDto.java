package de.symeda.sormas.ai.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

/**
 * Data Transfer Object for AI-powered outbreak predictions
 */
public class OutbreakPredictionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("predictionId")
    private String predictionId;

    @JsonProperty("region")
    private RegionReferenceDto region;

    @JsonProperty("disease")
    private Disease disease;

    @JsonProperty("riskScore")
    private Double riskScore; // 0.0 to 1.0

    @JsonProperty("riskLevel")
    private RiskLevel riskLevel;

    @JsonProperty("confidence")
    private Double confidence; // Model confidence 0.0 to 1.0

    @JsonProperty("predictedCases")
    private Integer predictedCases;

    @JsonProperty("predictionDate")
    private Date predictionDate;

    @JsonProperty("predictionHorizon")
    private Integer predictionHorizon; // Days ahead

    @JsonProperty("contributingFactors")
    private Map<String, Double> contributingFactors; // Feature importance

    @JsonProperty("recommendations")
    private String recommendations;

    @JsonProperty("modelVersion")
    private String modelVersion;

    @JsonProperty("alertLevel")
    private AlertLevel alertLevel;

    // Constructors
    public OutbreakPredictionDto() {
        this.predictionDate = new Date();
    }

    // Getters and Setters
    public String getPredictionId() {
        return predictionId;
    }

    public void setPredictionId(String predictionId) {
        this.predictionId = predictionId;
    }

    public RegionReferenceDto getRegion() {
        return region;
    }

    public void setRegion(RegionReferenceDto region) {
        this.region = region;
    }

    public Disease getDisease() {
        return disease;
    }

    public void setDisease(Disease disease) {
        this.disease = disease;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Integer getPredictedCases() {
        return predictedCases;
    }

    public void setPredictedCases(Integer predictedCases) {
        this.predictedCases = predictedCases;
    }

    public Date getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(Date predictionDate) {
        this.predictionDate = predictionDate;
    }

    public Integer getPredictionHorizon() {
        return predictionHorizon;
    }

    public void setPredictionHorizon(Integer predictionHorizon) {
        this.predictionHorizon = predictionHorizon;
    }

    public Map<String, Double> getContributingFactors() {
        return contributingFactors;
    }

    public void setContributingFactors(Map<String, Double> contributingFactors) {
        this.contributingFactors = contributingFactors;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    // Enums
    public enum RiskLevel {
        LOW,
        MODERATE,
        HIGH,
        CRITICAL
    }

    public enum AlertLevel {
        NONE,
        WATCH,
        WARNING,
        EMERGENCY
    }
}
