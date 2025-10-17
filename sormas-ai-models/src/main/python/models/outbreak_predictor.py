"""
Outbreak Prediction Model
Uses machine learning to predict outbreak risk and case counts
"""

import numpy as np
import uuid
from typing import Dict, Any
from datetime import datetime
import logging

logger = logging.getLogger(__name__)


class OutbreakPredictor:
    """
    ML model for outbreak prediction
    POC implementation with simple algorithms
    Will be enhanced with LSTM/Prophet for time-series forecasting
    """

    def __init__(self):
        self.model_version = "v1.0.0-poc"
        self.model_loaded = False
        self._load_model()

    def _load_model(self):
        """Load the ML model"""
        try:
            logger.info("Loading outbreak prediction model...")

            # POC: In production, load actual trained model
            # self.model = joblib.load('models/outbreak_predictor.pkl')

            self.model_loaded = True
            logger.info("Model loaded successfully")

        except Exception as e:
            logger.error(f"Error loading model: {str(e)}")
            self.model_loaded = False

    def predict(self, case_data: Dict[str, Any], horizon: int = 14) -> Dict[str, Any]:
        """
        Generate outbreak prediction

        Args:
            case_data: Historical case data
            horizon: Prediction horizon in days

        Returns:
            Prediction with risk score and forecasted cases
        """
        try:
            # Extract features
            recent_cases = case_data.get('recent_cases', 0)
            previous_cases = case_data.get('previous_cases', 0)
            population_density = case_data.get('population_density', 0)
            weekly_trend = case_data.get('weekly_trend', [])
            fatality_rate = case_data.get('fatality_rate', 0.0)

            # Calculate growth rate
            growth_rate = self._calculate_growth_rate(recent_cases, previous_cases)

            # Calculate trend acceleration
            trend_acceleration = self._calculate_trend_acceleration(weekly_trend)

            # Calculate risk score using weighted features
            risk_score = self._calculate_risk_score(
                recent_cases=recent_cases,
                growth_rate=growth_rate,
                population_density=population_density,
                trend_acceleration=trend_acceleration,
                fatality_rate=fatality_rate
            )

            # Predict future cases
            predicted_cases = self._predict_cases(
                recent_cases=recent_cases,
                growth_rate=growth_rate,
                horizon=horizon
            )

            # Calculate model confidence
            confidence = self._calculate_confidence(case_data, weekly_trend)

            # Determine contributing factors (feature importance)
            contributing_factors = self._get_contributing_factors(
                recent_cases=recent_cases,
                growth_rate=growth_rate,
                population_density=population_density,
                trend_acceleration=trend_acceleration
            )

            prediction = {
                'prediction_id': str(uuid.uuid4()),
                'risk_score': float(np.clip(risk_score, 0.0, 1.0)),
                'confidence': float(np.clip(confidence, 0.0, 1.0)),
                'predicted_cases': int(predicted_cases),
                'model_version': self.model_version,
                'contributing_factors': contributing_factors,
                'metadata': {
                    'growth_rate': float(growth_rate),
                    'trend_acceleration': float(trend_acceleration),
                    'horizon_days': horizon
                }
            }

            logger.info(f"Prediction generated: Risk={risk_score:.2f}, Cases={predicted_cases}")
            return prediction

        except Exception as e:
            logger.error(f"Prediction error: {str(e)}")
            raise

    def _calculate_growth_rate(self, recent: int, previous: int) -> float:
        """Calculate case growth rate"""
        if previous == 0:
            return 1.0 if recent > 0 else 0.0
        return (recent - previous) / previous

    def _calculate_trend_acceleration(self, weekly_trend: list) -> float:
        """Calculate acceleration in weekly trends"""
        if len(weekly_trend) < 2:
            return 0.0

        # Calculate week-over-week changes
        changes = [weekly_trend[i+1] - weekly_trend[i]
                  for i in range(len(weekly_trend)-1)]

        # Return average change rate
        return np.mean(changes) if changes else 0.0

    def _calculate_risk_score(
        self,
        recent_cases: int,
        growth_rate: float,
        population_density: int,
        trend_acceleration: float,
        fatality_rate: float
    ) -> float:
        """
        Calculate outbreak risk score
        POC: Using weighted sum of features
        Production: Will use trained ML model
        """

        # Normalize features
        case_score = min(recent_cases / 100, 1.0) * 0.30
        growth_score = min(max(growth_rate, 0) / 2.0, 1.0) * 0.25
        density_score = min(population_density / 2000, 1.0) * 0.20
        acceleration_score = min(max(trend_acceleration, 0) / 10, 1.0) * 0.15
        fatality_score = min(fatality_rate / 0.1, 1.0) * 0.10

        risk_score = (
            case_score +
            growth_score +
            density_score +
            acceleration_score +
            fatality_score
        )

        return risk_score

    def _predict_cases(self, recent_cases: int, growth_rate: float, horizon: int) -> int:
        """
        Predict future case count
        POC: Simple exponential growth model
        Production: Will use LSTM or Prophet
        """

        # Exponential growth projection
        daily_rate = growth_rate / 30  # Convert monthly to daily
        prediction_multiplier = (1 + daily_rate) ** horizon

        predicted = recent_cases * prediction_multiplier

        # Add some realistic bounds
        predicted = max(0, min(predicted, recent_cases * 10))

        return int(predicted)

    def _calculate_confidence(self, case_data: Dict, weekly_trend: list) -> float:
        """
        Calculate prediction confidence
        Based on data quality and consistency
        """

        confidence = 0.5  # Base confidence

        # More data points = higher confidence
        if len(weekly_trend) >= 4:
            confidence += 0.2

        # Consistent data = higher confidence
        if weekly_trend:
            variance = np.var(weekly_trend)
            if variance < 100:  # Low variance
                confidence += 0.15

        # Recent cases available
        if case_data.get('recent_cases', 0) > 0:
            confidence += 0.15

        return confidence

    def _get_contributing_factors(
        self,
        recent_cases: int,
        growth_rate: float,
        population_density: int,
        trend_acceleration: float
    ) -> Dict[str, float]:
        """
        Calculate feature importance (contributing factors)
        """

        total = (
            abs(recent_cases / 10) +
            abs(growth_rate * 100) +
            abs(population_density / 100) +
            abs(trend_acceleration * 10)
        )

        if total == 0:
            total = 1

        return {
            "Recent Case Volume": round(abs(recent_cases / 10) / total, 3),
            "Growth Rate": round(abs(growth_rate * 100) / total, 3),
            "Population Density": round(abs(population_density / 100) / total, 3),
            "Trend Acceleration": round(abs(trend_acceleration * 10) / total, 3),
            "Seasonal Factors": 0.05,  # Placeholder
            "Vaccination Coverage": 0.03  # Placeholder
        }

    def is_loaded(self) -> bool:
        """Check if model is loaded"""
        return self.model_loaded

    def get_version(self) -> str:
        """Get model version"""
        return self.model_version
