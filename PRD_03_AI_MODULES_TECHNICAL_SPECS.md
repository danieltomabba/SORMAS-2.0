# Project Requirements Document (PRD)
# AI-Enhanced SORMAS: Outbreak Prediction System
## Part 3: AI Modules Technical Specifications

---

## Document Information

| Field | Value |
|-------|-------|
| **Document** | PRD Part 3 of 8 |
| **Version** | 1.0.0 |
| **Date** | October 17, 2025 |
| **Dependencies** | PRD_01, PRD_02 |

---

## Table of Contents

1. [Module Overview](#1-module-overview)
2. [sormas-ai-api Module](#2-sormas-ai-api-module)
3. [sormas-ai-backend Module](#3-sormas-ai-backend-module)
4. [sormas-ai-models Module (Python)](#4-sormas-ai-models-module-python)
5. [sormas-flow Module](#5-sormas-flow-module)
6. [ML Models and Algorithms](#6-ml-models-and-algorithms)
7. [Performance Requirements](#7-performance-requirements)
8. [Error Handling](#8-error-handling)

---

## 1. Module Overview

### 1.1 AI Modules Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    New AI Modules                            │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ sormas-ai-api│  │sormas-ai-back│  │ sormas-flow  │      │
│  │  (DTOs)      │◄─┤  (EJB Logic) │◄─┤  (UI)        │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                                 │
│         │                  ▼                                 │
│         │        ┌──────────────────┐                        │
│         │        │ sormas-ai-models │                        │
│         │        │  (Python ML)     │                        │
│         │        └──────────────────┘                        │
│         │                  │                                 │
│         ▼                  ▼                                 │
│  ┌──────────────────────────────────┐                       │
│  │     Existing SORMAS Backend      │                       │
│  │  (Cases, Regions, Population)    │                       │
│  └──────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Module Dependencies

```xml
sormas-flow
  └─depends on─► sormas-ai-api
                   └─depends on─► sormas-api

sormas-ai-backend
  ├─depends on─► sormas-ai-api
  ├─depends on─► sormas-backend
  └─depends on─► sormas-api

sormas-ai-models (Python)
  └─HTTP calls to─► sormas-ai-backend
```

---

## 2. sormas-ai-api Module

### 2.1 Module Configuration

**Location**: `/sormas-ai-api`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>de.symeda.sormas</groupId>
        <artifactId>sormas-base</artifactId>
        <version>1.103.0-SNAPSHOT</version>
        <relativePath>../sormas-base</relativePath>
    </parent>

    <artifactId>sormas-ai-api</artifactId>
    <packaging>jar</packaging>
    <name>SORMAS AI API</name>

    <dependencies>
        <!-- SORMAS Core API -->
        <dependency>
            <groupId>de.symeda.sormas</groupId>
            <artifactId>sormas-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Java EE -->
        <dependency>
            <groupId>javax</groupId>
            <artifactId>javaee-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 2.2 Package Structure

```
sormas-ai-api/src/main/java/de/symeda/sormas/ai/api/
├── AIOutbreakPredictionFacade.java         # Main facade interface
├── dto/
│   ├── OutbreakPredictionDto.java          # Prediction DTO
│   ├── AIAnalysisRequestDto.java           # Analysis request DTO
│   ├── ModelVersionDto.java                # Model version info
│   └── PredictionMetricsDto.java           # Model performance metrics
└── enums/
    ├── RiskLevel.java                      # LOW, MODERATE, HIGH, CRITICAL
    ├── AlertLevel.java                     # NONE, WATCH, WARNING, EMERGENCY
    └── AnalysisType.java                   # Prediction types
```

### 2.3 Core Interfaces

#### AIOutbreakPredictionFacade.java

```java
package de.symeda.sormas.ai.api;

import java.util.List;
import javax.ejb.Remote;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.ai.api.dto.AIAnalysisRequestDto;
import de.symeda.sormas.ai.api.dto.OutbreakPredictionDto;

/**
 * EJB Remote Interface for AI Outbreak Prediction Services
 *
 * This facade provides methods for:
 * - Predicting disease outbreaks using ML models
 * - Assessing outbreak risk levels
 * - Managing AI model versions
 * - Retrieving prediction history
 *
 * @author SORMAS AI Team
 * @since 1.103.0
 */
@Remote
public interface AIOutbreakPredictionFacade {

    /**
     * Predict outbreak for a specific region and disease
     *
     * @param region The region to predict for
     * @param disease The disease to analyze
     * @param predictionHorizon Days ahead to predict (7, 14, or 30)
     * @return Outbreak prediction with risk score and recommendations
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if prediction service is unavailable
     */
    OutbreakPredictionDto predictOutbreak(
        RegionReferenceDto region,
        Disease disease,
        Integer predictionHorizon
    );

    /**
     * Get current predictions for a disease across all regions
     *
     * @param disease The disease to get predictions for
     * @return List of predictions ordered by risk score (descending)
     */
    List<OutbreakPredictionDto> getCurrentPredictions(Disease disease);

    /**
     * Get predictions filtered by risk level
     *
     * @param riskLevel The minimum risk level to filter by
     * @param disease Optional disease filter (null for all)
     * @return List of predictions matching criteria
     */
    List<OutbreakPredictionDto> getPredictionsByRiskLevel(
        OutbreakPredictionDto.RiskLevel riskLevel,
        Disease disease
    );

    /**
     * Get prediction by unique ID
     *
     * @param predictionId The prediction UUID
     * @return The prediction or null if not found
     */
    OutbreakPredictionDto getPredictionById(String predictionId);

    /**
     * Perform comprehensive AI analysis across multiple regions/diseases
     *
     * @param request Analysis parameters
     * @return List of predictions for all requested combinations
     */
    List<OutbreakPredictionDto> performAnalysis(AIAnalysisRequestDto request);

    /**
     * Trigger ML model retraining with latest data
     *
     * @return Status message with training job ID
     * @throws RuntimeException if training service is unavailable
     */
    String retrainModels();

    /**
     * Get available ML model versions
     *
     * @return List of model versions with metadata
     */
    List<ModelVersionDto> getAvailableModelVersions();

    /**
     * Get model performance metrics
     *
     * @param modelVersion Optional version filter
     * @return Model accuracy, precision, recall, etc.
     */
    PredictionMetricsDto getModelMetrics(String modelVersion);
}
```

### 2.4 Data Transfer Objects

#### OutbreakPredictionDto.java

```java
package de.symeda.sormas.ai.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.region.RegionReferenceDto;

/**
 * DTO for AI outbreak predictions
 *
 * Contains:
 * - Prediction identification
 * - Risk assessment (score, level, confidence)
 * - Predicted case counts
 * - Contributing factors with importance scores
 * - Actionable recommendations
 * - Model metadata
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutbreakPredictionDto extends EntityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Identification
    private String predictionId;
    private RegionReferenceDto region;
    private Disease disease;

    // Risk Assessment
    private Double riskScore;              // 0.0 - 1.0
    private Double confidence;             // 0.0 - 1.0
    private RiskLevel riskLevel;
    private AlertLevel alertLevel;

    // Predictions
    private Integer predictedCases;
    private Integer predictionHorizon;     // Days ahead
    private Integer currentCases;
    private Double growthRate;

    // Analysis
    private Map<String, Double> contributingFactors;  // Factor -> Importance
    private String recommendations;

    // Metadata
    private String modelVersion;
    private Date predictionTimestamp;
    private Date validUntil;

    // Enums
    public enum RiskLevel {
        LOW,        // Risk score < 0.3
        MODERATE,   // Risk score 0.3 - 0.6
        HIGH,       // Risk score 0.6 - 0.8
        CRITICAL    // Risk score > 0.8
    }

    public enum AlertLevel {
        NONE,       // Normal monitoring
        WATCH,      // Increased surveillance
        WARNING,    // Prepare response
        EMERGENCY   // Activate response
    }

    // Constructors
    public OutbreakPredictionDto() {
        super();
    }

    public OutbreakPredictionDto(String uuid) {
        super(uuid);
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
        // Auto-calculate risk level based on score
        if (riskScore != null) {
            if (riskScore < 0.3) {
                this.riskLevel = RiskLevel.LOW;
            } else if (riskScore < 0.6) {
                this.riskLevel = RiskLevel.MODERATE;
            } else if (riskScore < 0.8) {
                this.riskLevel = RiskLevel.HIGH;
            } else {
                this.riskLevel = RiskLevel.CRITICAL;
            }
        }
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Integer getPredictedCases() {
        return predictedCases;
    }

    public void setPredictedCases(Integer predictedCases) {
        this.predictedCases = predictedCases;
    }

    public Integer getPredictionHorizon() {
        return predictionHorizon;
    }

    public void setPredictionHorizon(Integer predictionHorizon) {
        this.predictionHorizon = predictionHorizon;
    }

    public Integer getCurrentCases() {
        return currentCases;
    }

    public void setCurrentCases(Integer currentCases) {
        this.currentCases = currentCases;
    }

    public Double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(Double growthRate) {
        this.growthRate = growthRate;
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

    public Date getPredictionTimestamp() {
        return predictionTimestamp;
    }

    public void setPredictionTimestamp(Date predictionTimestamp) {
        this.predictionTimestamp = predictionTimestamp;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return "OutbreakPredictionDto{" +
                "predictionId='" + predictionId + '\'' +
                ", region=" + region +
                ", disease=" + disease +
                ", riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", predictedCases=" + predictedCases +
                ", confidence=" + confidence +
                '}';
    }
}
```

#### AIAnalysisRequestDto.java

```java
package de.symeda.sormas.ai.api.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.region.RegionReferenceDto;

/**
 * DTO for requesting comprehensive AI analysis
 *
 * Allows batch prediction across:
 * - Multiple regions
 * - Multiple diseases
 * - Multiple time horizons
 */
public class AIAnalysisRequestDto extends EntityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private AnalysisType analysisType;
    private List<RegionReferenceDto> regions;
    private List<Disease> diseases;
    private Integer predictionHorizon;
    private Date startDate;
    private Date endDate;
    private String modelVersion;
    private Boolean includeRecommendations;

    public enum AnalysisType {
        OUTBREAK_PREDICTION,        // Standard prediction
        RISK_ASSESSMENT,            // Risk scoring only
        TREND_ANALYSIS,             // Time-series trends
        COMPARATIVE_ANALYSIS,       // Compare regions
        SCENARIO_MODELING           // What-if scenarios
    }

    // Constructors
    public AIAnalysisRequestDto() {
        super();
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

    public Integer getPredictionHorizon() {
        return predictionHorizon;
    }

    public void setPredictionHorizon(Integer predictionHorizon) {
        this.predictionHorizon = predictionHorizon;
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

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Boolean getIncludeRecommendations() {
        return includeRecommendations;
    }

    public void setIncludeRecommendations(Boolean includeRecommendations) {
        this.includeRecommendations = includeRecommendations;
    }
}
```

### 2.5 API Requirements

**REQ-API-001**: All DTOs must be JSON serializable
**REQ-API-002**: All DTOs must extend `EntityDto` or implement `Serializable`
**REQ-API-003**: Enums must be string-based for JSON compatibility
**REQ-API-004**: All facade methods must have JavaDoc comments
**REQ-API-005**: DTOs must include validation annotations where applicable

---

## 3. sormas-ai-backend Module

### 3.1 Module Configuration

**Location**: `/sormas-ai-backend`

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>de.symeda.sormas</groupId>
        <artifactId>sormas-base</artifactId>
        <version>1.103.0-SNAPSHOT</version>
        <relativePath>../sormas-base</relativePath>
    </parent>

    <artifactId>sormas-ai-backend</artifactId>
    <packaging>ejb</packaging>
    <name>SORMAS AI Backend</name>

    <dependencies>
        <!-- AI API -->
        <dependency>
            <groupId>de.symeda.sormas</groupId>
            <artifactId>sormas-ai-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- SORMAS Core -->
        <dependency>
            <groupId>de.symeda.sormas</groupId>
            <artifactId>sormas-backend</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Java EE -->
        <dependency>
            <groupId>javax</groupId>
            <artifactId>javaee-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- HTTP Client (Java 11+) -->
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.10.0</version>
        </dependency>

        <!-- JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ejb-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <ejbVersion>3.2</ejbVersion>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 Package Structure

```
sormas-ai-backend/src/main/java/de/symeda/sormas/ai/backend/
├── AIOutbreakPredictionFacadeEjb.java      # Main EJB implementation
├── AIModelService.java                     # HTTP client to Python service
├── CaseDataAnalysisService.java            # POC algorithms
├── RiskAssessmentService.java              # Risk calculation
└── util/
    ├── PredictionCache.java                # Cache predictions
    └── AIConfigurationHelper.java          # Read config
```

### 3.3 EJB Implementation

#### AIOutbreakPredictionFacadeEjb.java

```java
package de.symeda.sormas.ai.backend;

import java.util.*;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.*;
import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.PopulationDataFacade;
import de.symeda.sormas.api.region.RegionFacade;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.ai.api.AIOutbreakPredictionFacade;
import de.symeda.sormas.ai.api.dto.*;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;

/**
 * EJB implementation of AI Outbreak Prediction services
 *
 * This implementation:
 * - Retrieves case data from SORMAS backend
 * - Calls Python ML service for predictions
 * - Caches results for performance
 * - Provides fallback POC algorithms
 *
 * @author SORMAS AI Team
 * @since 1.103.0
 */
@Stateless(name = "AIOutbreakPredictionFacade")
public class AIOutbreakPredictionFacadeEjb implements AIOutbreakPredictionFacade {

    private static final Logger logger = LoggerFactory.getLogger(AIOutbreakPredictionFacadeEjb.class);

    @EJB
    private CaseFacadeEjb caseFacade;

    @EJB
    private RegionFacade regionFacade;

    @EJB
    private PopulationDataFacade populationDataFacade;

    @EJB
    private AIModelService aiModelService;

    @EJB
    private CaseDataAnalysisService caseDataAnalysisService;

    @Override
    public OutbreakPredictionDto predictOutbreak(
            RegionReferenceDto region,
            Disease disease,
            Integer predictionHorizon) {

        logger.info("Predicting outbreak for region: {}, disease: {}, horizon: {} days",
                region.getCaption(), disease, predictionHorizon);

        try {
            // 1. Validate inputs
            validatePredictionRequest(region, disease, predictionHorizon);

            // 2. Get case data
            CaseDataAnalysisService.CaseDataSummary caseData =
                caseDataAnalysisService.analyzeCaseData(region, disease);

            // 3. Get population data
            PopulationDataCriteria popCriteria = new PopulationDataCriteria();
            popCriteria.region(region);
            List<PopulationDataDto> popData = populationDataFacade.getPopulationData(popCriteria);
            int totalPopulation = popData.stream()
                .mapToInt(PopulationDataDto::getPopulation)
                .sum();

            // 4. Try ML service first
            OutbreakPredictionDto prediction;
            try {
                prediction = aiModelService.requestPrediction(
                    region, disease, caseData, totalPopulation, predictionHorizon
                );
                logger.info("ML service prediction successful");
            } catch (Exception e) {
                // 5. Fallback to POC algorithm
                logger.warn("ML service unavailable, using POC algorithm: {}", e.getMessage());
                prediction = createPOCPrediction(
                    region, disease, caseData, totalPopulation, predictionHorizon
                );
            }

            // 6. Enrich with recommendations
            enrichWithRecommendations(prediction);

            logger.info("Prediction completed: risk score = {}, predicted cases = {}",
                    prediction.getRiskScore(), prediction.getPredictedCases());

            return prediction;

        } catch (Exception e) {
            logger.error("Error predicting outbreak", e);
            throw new RuntimeException("Outbreak prediction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OutbreakPredictionDto> getCurrentPredictions(Disease disease) {
        logger.info("Getting current predictions for disease: {}", disease);

        List<RegionReferenceDto> regions = regionFacade.getAllActiveAsReference();
        List<OutbreakPredictionDto> predictions = new ArrayList<>();

        for (RegionReferenceDto region : regions) {
            try {
                OutbreakPredictionDto prediction = predictOutbreak(region, disease, 14);
                predictions.add(prediction);
            } catch (Exception e) {
                logger.warn("Failed to predict for region {}: {}",
                        region.getCaption(), e.getMessage());
            }
        }

        // Sort by risk score descending
        predictions.sort((p1, p2) ->
            Double.compare(p2.getRiskScore(), p1.getRiskScore())
        );

        return predictions;
    }

    @Override
    public List<OutbreakPredictionDto> getPredictionsByRiskLevel(
            OutbreakPredictionDto.RiskLevel riskLevel,
            Disease disease) {

        List<OutbreakPredictionDto> allPredictions = getCurrentPredictions(disease);

        return allPredictions.stream()
            .filter(p -> p.getRiskLevel().ordinal() >= riskLevel.ordinal())
            .collect(Collectors.toList());
    }

    @Override
    public String retrainModels() {
        logger.info("Triggering model retraining");
        try {
            String jobId = aiModelService.triggerRetraining();
            return "Model retraining initiated. Job ID: " + jobId;
        } catch (Exception e) {
            logger.error("Failed to trigger retraining", e);
            throw new RuntimeException("Model retraining failed: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void validatePredictionRequest(
            RegionReferenceDto region,
            Disease disease,
            Integer predictionHorizon) {

        if (region == null || region.getUuid() == null) {
            throw new IllegalArgumentException("Region is required");
        }

        if (disease == null) {
            throw new IllegalArgumentException("Disease is required");
        }

        if (predictionHorizon == null || predictionHorizon < 1 || predictionHorizon > 90) {
            throw new IllegalArgumentException("Prediction horizon must be between 1 and 90 days");
        }
    }

    private OutbreakPredictionDto createPOCPrediction(
            RegionReferenceDto region,
            Disease disease,
            CaseDataAnalysisService.CaseDataSummary caseData,
            int population,
            int horizon) {

        OutbreakPredictionDto prediction = new OutbreakPredictionDto();
        prediction.setPredictionId(UUID.randomUUID().toString());
        prediction.setRegion(region);
        prediction.setDisease(disease);
        prediction.setPredictionHorizon(horizon);
        prediction.setModelVersion("v1.0.0-poc");
        prediction.setPredictionTimestamp(new Date());

        // Calculate risk score (POC algorithm)
        double riskScore = calculatePOCRiskScore(caseData, population);
        prediction.setRiskScore(riskScore);
        prediction.setConfidence(0.70); // POC confidence

        // Predict cases (simple exponential growth)
        int predictedCases = (int) (caseData.getRecentCases() *
            Math.pow(1 + caseData.getGrowthRate(), horizon / 7.0));
        prediction.setPredictedCases(predictedCases);
        prediction.setCurrentCases(caseData.getRecentCases());
        prediction.setGrowthRate(caseData.getGrowthRate());

        // Alert level
        prediction.setAlertLevel(determineAlertLevel(riskScore, caseData.getGrowthRate()));

        // Contributing factors
        Map<String, Double> factors = new LinkedHashMap<>();
        factors.put("Recent Case Volume", 0.35);
        factors.put("Growth Rate", 0.25);
        factors.put("Population Density", 0.20);
        factors.put("Trend Acceleration", 0.15);
        factors.put("Historical Patterns", 0.05);
        prediction.setContributingFactors(factors);

        return prediction;
    }

    private double calculatePOCRiskScore(
            CaseDataAnalysisService.CaseDataSummary caseData,
            int population) {

        // Weighted scoring
        double caseVolumeScore = Math.min(1.0, caseData.getRecentCases() / (population * 0.001));
        double growthScore = Math.min(1.0, Math.max(0, caseData.getGrowthRate() * 2));
        double trendScore = caseData.getWeeklyTrend().size() > 1 ?
            (caseData.getWeeklyTrend().get(caseData.getWeeklyTrend().size() - 1) /
             (double) caseData.getWeeklyTrend().get(0)) - 1.0 : 0.0;
        trendScore = Math.min(1.0, Math.max(0, trendScore));

        return (caseVolumeScore * 0.4) + (growthScore * 0.35) + (trendScore * 0.25);
    }

    private OutbreakPredictionDto.AlertLevel determineAlertLevel(
            double riskScore,
            double growthRate) {

        if (riskScore > 0.8 || growthRate > 0.5) {
            return OutbreakPredictionDto.AlertLevel.EMERGENCY;
        } else if (riskScore > 0.6 || growthRate > 0.3) {
            return OutbreakPredictionDto.AlertLevel.WARNING;
        } else if (riskScore > 0.3 || growthRate > 0.1) {
            return OutbreakPredictionDto.AlertLevel.WATCH;
        }
        return OutbreakPredictionDto.AlertLevel.NONE;
    }

    private void enrichWithRecommendations(OutbreakPredictionDto prediction) {
        StringBuilder recommendations = new StringBuilder();

        switch (prediction.getRiskLevel()) {
            case CRITICAL:
                recommendations.append("• Activate emergency response protocols\n");
                recommendations.append("• Deploy rapid response teams\n");
                recommendations.append("• Increase surveillance intensity\n");
                recommendations.append("• Prepare isolation facilities\n");
                break;
            case HIGH:
                recommendations.append("• Enhance active case finding\n");
                recommendations.append("• Strengthen contact tracing\n");
                recommendations.append("• Review stock of medical supplies\n");
                recommendations.append("• Alert healthcare facilities\n");
                break;
            case MODERATE:
                recommendations.append("• Monitor case trends closely\n");
                recommendations.append("• Verify case investigation completeness\n");
                recommendations.append("• Review preventive measures\n");
                break;
            case LOW:
                recommendations.append("• Continue routine surveillance\n");
                recommendations.append("• Maintain readiness protocols\n");
                break;
        }

        prediction.setRecommendations(recommendations.toString());
    }
}
```

### 3.4 Backend Requirements

**REQ-BACKEND-001**: Must not modify existing SORMAS tables
**REQ-BACKEND-002**: Must use @EJB injection for SORMAS facades
**REQ-BACKEND-003**: Must handle ML service unavailability gracefully
**REQ-BACKEND-004**: Must log all prediction requests and results
**REQ-BACKEND-005**: Must validate all inputs before processing
**REQ-BACKEND-006**: Must implement caching for frequently requested predictions
**REQ-BACKEND-007**: Must complete predictions within 2 seconds (95th percentile)

---

## 4. sormas-ai-models Module (Python)

### 4.1 Module Structure

**Location**: `/sormas-ai-models`

```
sormas-ai-models/
├── Dockerfile
├── requirements.txt
├── .env.example
├── README.md
├── src/
│   └── main/
│       └── python/
│           ├── app.py                      # FastAPI application
│           ├── config.py                   # Configuration
│           ├── models/
│           │   ├── __init__.py
│           │   ├── outbreak_predictor.py   # Main ML model
│           │   ├── risk_assessor.py        # Risk scoring
│           │   ├── model_trainer.py        # Training pipeline
│           │   └── feature_engineering.py  # Feature extraction
│           ├── api/
│           │   ├── __init__.py
│           │   ├── predict_routes.py       # Prediction endpoints
│           │   ├── admin_routes.py         # Admin endpoints
│           │   └── schemas.py              # Pydantic models
│           └── utils/
│               ├── database.py             # DB connection
│               ├── mlflow_client.py        # MLflow integration
│               └── logger.py               # Logging setup
├── tests/
│   ├── test_outbreak_predictor.py
│   ├── test_risk_assessor.py
│   └── test_api.py
└── models/
    └── trained/                            # Stored models
        └── .gitkeep
```

### 4.2 Python Requirements

**requirements.txt**:
```txt
# Web Framework
fastapi==0.104.1
uvicorn[standard]==0.24.0
python-multipart==0.0.6

# Machine Learning
scikit-learn==1.3.2
numpy==1.24.3
pandas==2.1.3
scipy==1.11.4

# Deep Learning (optional, for future)
tensorflow==2.14.0
torch==2.1.0
transformers==4.35.0

# Time Series
prophet==1.1.5
statsmodels==0.14.0

# MLOps
mlflow==2.8.1
optuna==3.4.0

# Database
psycopg2-binary==2.9.9
sqlalchemy==2.0.23
alembic==1.12.1

# Utilities
python-dotenv==1.0.0
pydantic==2.5.0
pydantic-settings==2.1.0
requests==2.31.0

# Logging and Monitoring
loguru==0.7.2
prometheus-client==0.19.0

# Testing
pytest==7.4.3
pytest-asyncio==0.21.1
pytest-cov==4.1.0
httpx==0.25.1

# Data Visualization (for notebooks)
matplotlib==3.8.2
seaborn==0.13.0
plotly==5.18.0
```

### 4.3 FastAPI Application

#### app.py

```python
"""
SORMAS AI Models - FastAPI Application

This service provides ML-powered outbreak prediction capabilities
through RESTful APIs.

Endpoints:
- POST /predict/outbreak: Generate outbreak predictions
- POST /assess/risk: Assess outbreak risk levels
- GET /models/versions: List available model versions
- POST /models/retrain: Trigger model retraining
- GET /health: Health check
- GET /metrics: Prometheus metrics
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from prometheus_client import make_asgi_app
import uvicorn
from loguru import logger

from api import predict_routes, admin_routes
from config import settings
from utils.database import init_db
from utils.mlflow_client import init_mlflow

# Initialize FastAPI app
app = FastAPI(
    title="SORMAS AI Prediction Service",
    description="Machine Learning service for outbreak prediction and risk assessment",
    version="1.0.0-POC",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # TODO: Restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Prometheus metrics
metrics_app = make_asgi_app()
app.mount("/metrics", metrics_app)

# Include routers
app.include_router(predict_routes.router, prefix="/predict", tags=["Predictions"])
app.include_router(admin_routes.router, prefix="/models", tags=["Model Management"])

@app.on_event("startup")
async def startup_event():
    """Initialize services on startup"""
    logger.info("Starting SORMAS AI Prediction Service")

    # Initialize database connection
    try:
        init_db()
        logger.info("Database connection established")
    except Exception as e:
        logger.error(f"Database initialization failed: {e}")
        raise

    # Initialize MLflow
    try:
        init_mlflow()
        logger.info(f"MLflow tracking URI: {settings.MLFLOW_TRACKING_URI}")
    except Exception as e:
        logger.warning(f"MLflow initialization failed: {e}")

    logger.info("SORMAS AI Service started successfully")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown"""
    logger.info("Shutting down SORMAS AI Prediction Service")

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "SORMAS AI Service",
        "version": "v1.0.0-poc"
    }

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "SORMAS AI Prediction Service",
        "docs": "/docs",
        "health": "/health",
        "metrics": "/metrics"
    }

if __name__ == "__main__":
    uvicorn.run(
        "app:app",
        host=settings.API_HOST,
        port=settings.API_PORT,
        reload=settings.DEBUG,
        log_level=settings.LOG_LEVEL.lower()
    )
```

### 4.4 Python Module Requirements

**REQ-PY-001**: Must use Python 3.11+
**REQ-PY-002**: Must follow PEP 8 style guidelines
**REQ-PY-003**: Must include type hints for all functions
**REQ-PY-004**: Must handle database connection failures
**REQ-PY-005**: Must log all predictions to MLflow
**REQ-PY-006**: Must validate all input schemas with Pydantic
**REQ-PY-007**: Must return predictions within 1.5 seconds
**REQ-PY-008**: Must include unit tests with >80% coverage

---

## 5. sormas-flow Module

### 5.1 Module Configuration

**Location**: `/sormas-flow`

**pom.xml** (key sections):
```xml
<properties>
    <vaadin.version>24.3.0</vaadin.version>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
</properties>

<dependencies>
    <!-- Vaadin Flow -->
    <dependency>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-core</artifactId>
        <version>${vaadin.version}</version>
    </dependency>

    <!-- AI API -->
    <dependency>
        <groupId>de.symeda.sormas</groupId>
        <artifactId>sormas-ai-api</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- CDI for injection -->
    <dependency>
        <groupId>javax.enterprise</groupId>
        <artifactId>cdi-api</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Vaadin Maven Plugin -->
        <plugin>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-maven-plugin</artifactId>
            <version>${vaadin.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-frontend</goal>
                        <goal>build-frontend</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**package.json**:
```json
{
  "name": "sormas-flow",
  "version": "1.103.0",
  "dependencies": {
    "@vaadin/accordion": "24.3.0",
    "@vaadin/button": "24.3.0",
    "@vaadin/charts": "24.3.0",
    "@vaadin/combo-box": "24.3.0",
    "@vaadin/grid": "24.3.0",
    "@vaadin/notification": "24.3.0",
    "highcharts": "^11.2.0"
  }
}
```

### 5.2 Flow Requirements

**REQ-FLOW-001**: Must use Vaadin Flow 24.x
**REQ-FLOW-002**: Must not depend on Vaadin 8 components
**REQ-FLOW-003**: Must be deployable as standalone WAR
**REQ-FLOW-004**: Must use CDI for EJB injection
**REQ-FLOW-005**: Dashboard must load in < 3 seconds
**REQ-FLOW-006**: Must be responsive (mobile-friendly)
**REQ-FLOW-007**: Must follow Vaadin Lumo theme

---

## 6. ML Models and Algorithms

### 6.1 POC Algorithm (Current)

**Method**: Weighted Feature Scoring

```python
def calculate_risk_score(case_data, population):
    # Feature extraction
    recent_cases = case_data['recent_cases']
    growth_rate = case_data['growth_rate']
    population_density = population / area

    # Normalization
    case_volume_score = min(1.0, recent_cases / (population * 0.001))
    growth_score = min(1.0, max(0, growth_rate * 2))
    density_score = min(1.0, population_density / 10000)

    # Weighted combination
    risk_score = (
        case_volume_score * 0.4 +
        growth_score * 0.35 +
        density_score * 0.25
    )

    return risk_score
```

### 6.2 Future ML Models

#### Phase 2: Advanced Models

1. **LSTM (Long Short-Term Memory)**
   - Purpose: Time-series forecasting
   - Input: 60-day case history
   - Output: 30-day predictions
   - Accuracy target: MAPE < 15%

2. **Prophet (Facebook)**
   - Purpose: Seasonal pattern detection
   - Input: 1-year case history
   - Output: Trend + seasonality
   - Accuracy target: MAE < 20 cases

3. **Random Forest**
   - Purpose: Feature importance
   - Input: Cases + weather + mobility
   - Output: Risk classification
   - Accuracy target: F1 > 0.85

#### Phase 3: Production Models

1. **Transformer-based**
   - Multi-disease attention
   - External data integration
   - Explainable predictions

2. **Ensemble**
   - Combine multiple models
   - Weighted voting
   - Uncertainty quantification

### 6.3 Model Performance Metrics

| Metric | POC Target | Phase 2 Target | Phase 3 Target |
|--------|------------|----------------|----------------|
| Accuracy | ±20% | ±15% | ±10% |
| MAPE | <25% | <15% | <10% |
| MAE | <50 cases | <25 cases | <15 cases |
| F1 Score | >0.70 | >0.85 | >0.90 |
| Response Time | <2s | <1.5s | <1s |
| Confidence | 0.60-0.80 | 0.75-0.90 | 0.85-0.95 |

---

## 7. Performance Requirements

### 7.1 Response Times

| Operation | P50 | P95 | P99 | Timeout |
|-----------|-----|-----|-----|---------|
| Single prediction | 500ms | 1.5s | 2s | 5s |
| Batch predictions (10 regions) | 2s | 5s | 8s | 15s |
| Dashboard load | 1s | 2.5s | 3s | 10s |
| Model retraining | N/A | N/A | N/A | 5min |

### 7.2 Throughput

- Concurrent users: 100+
- Predictions per minute: 500+
- API requests per second: 50+

### 7.3 Resource Usage

| Component | CPU | Memory | Disk |
|-----------|-----|--------|------|
| Java Backend | 2 cores | 2GB | 1GB |
| Python ML Service | 4 cores | 4GB | 5GB |
| PostgreSQL | 2 cores | 4GB | 20GB |
| MLflow | 1 core | 2GB | 10GB |

---

## 8. Error Handling

### 8.1 Error Types

```java
public class AIServiceException extends RuntimeException {
    public enum ErrorType {
        SERVICE_UNAVAILABLE,
        INVALID_REQUEST,
        MODEL_ERROR,
        DATA_INSUFFICIENT,
        TIMEOUT
    }
}
```

### 8.2 Fallback Strategy

1. **Primary**: Call Python ML service
2. **Fallback**: Use POC algorithm in Java
3. **Cache**: Return last valid prediction (if < 24h old)
4. **Error**: Return error with explanation

### 8.3 Logging Requirements

**REQ-LOG-001**: Log all prediction requests with region, disease, timestamp
**REQ-LOG-002**: Log all ML service calls (success/failure)
**REQ-LOG-003**: Log all errors with full stack trace
**REQ-LOG-004**: Log performance metrics (response time, model version)
**REQ-LOG-005**: Use structured logging (JSON format)

---

**End of Part 3: AI Modules Technical Specifications**

**Next Document**: PRD_04_API_CONTRACTS_INTEGRATION.md
