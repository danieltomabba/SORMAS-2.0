package de.symeda.sormas.ai.api;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import de.symeda.sormas.ai.api.dto.AIAnalysisRequestDto;
import de.symeda.sormas.ai.api.dto.OutbreakPredictionDto;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

/**
 * Facade for AI-powered outbreak prediction services
 */
@Remote
public interface AIOutbreakPredictionFacade {

    /**
     * Get outbreak prediction for a specific region and disease
     *
     * @param region Region to analyze
     * @param disease Disease to predict
     * @param predictionHorizon Days ahead to predict
     * @return Outbreak prediction with risk scores and recommendations
     */
    OutbreakPredictionDto predictOutbreak(RegionReferenceDto region, Disease disease, Integer predictionHorizon);

    /**
     * Get outbreak predictions for multiple regions
     *
     * @param regions List of regions to analyze
     * @param disease Disease to predict
     * @param predictionHorizon Days ahead to predict
     * @return List of outbreak predictions
     */
    List<OutbreakPredictionDto> predictOutbreaks(List<RegionReferenceDto> regions, Disease disease, Integer predictionHorizon);

    /**
     * Get all current outbreak predictions for a specific disease
     *
     * @param disease Disease to analyze
     * @return List of current predictions
     */
    List<OutbreakPredictionDto> getCurrentPredictions(Disease disease);

    /**
     * Get outbreak predictions by risk level
     *
     * @param riskLevel Minimum risk level to include
     * @param disease Optional disease filter
     * @return List of predictions matching criteria
     */
    List<OutbreakPredictionDto> getPredictionsByRiskLevel(
        OutbreakPredictionDto.RiskLevel riskLevel,
        Disease disease
    );

    /**
     * Perform comprehensive AI analysis
     *
     * @param request Analysis request with parameters
     * @return List of predictions based on request
     */
    List<OutbreakPredictionDto> performAnalysis(AIAnalysisRequestDto request);

    /**
     * Get historical accuracy of predictions
     *
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Accuracy metrics
     */
    Double getModelAccuracy(Date startDate, Date endDate);

    /**
     * Retrain AI models with latest data
     * This is an admin operation that triggers model retraining
     *
     * @return Status message
     */
    String retrainModels();

    /**
     * Get available AI model versions
     *
     * @return List of model version identifiers
     */
    List<String> getAvailableModelVersions();
}
