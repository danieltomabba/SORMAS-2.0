package de.symeda.sormas.ai.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

/**
 * Request DTO for AI analysis
 */
public class AIAnalysisRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("analysisType")
    private AnalysisType analysisType;

    @JsonProperty("regions")
    private List<RegionReferenceDto> regions;

    @JsonProperty("diseases")
    private List<Disease> diseases;

    @JsonProperty("startDate")
    private Date startDate;

    @JsonProperty("endDate")
    private Date endDate;

    @JsonProperty("predictionHorizon")
    private Integer predictionHorizon; // Days ahead

    @JsonProperty("includeRecommendations")
    private Boolean includeRecommendations;

    @JsonProperty("modelVersion")
    private String modelVersion;

    // Constructors
    public AIAnalysisRequestDto() {
        this.includeRecommendations = true;
    }

    // Getters and Setters
    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public List<RegionReferenceDto> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionReferenceDto> regions) {
        this.regions = regions;
    }

    public List<Disease> getDiseases() {
        return diseases;
    }

    public void setDiseases(List<Disease> diseases) {
        this.diseases = diseases;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getPredictionHorizon() {
        return predictionHorizon;
    }

    public void setPredictionHorizon(Integer predictionHorizon) {
        this.predictionHorizon = predictionHorizon;
    }

    public Boolean getIncludeRecommendations() {
        return includeRecommendations;
    }

    public void setIncludeRecommendations(Boolean includeRecommendations) {
        this.includeRecommendations = includeRecommendations;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    // Enums
    public enum AnalysisType {
        OUTBREAK_PREDICTION,
        RISK_ASSESSMENT,
        TREND_ANALYSIS,
        CONTACT_TRACING_OPTIMIZATION,
        RESOURCE_ALLOCATION
    }
}
