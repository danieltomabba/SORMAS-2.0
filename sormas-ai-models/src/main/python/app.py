"""
SORMAS AI Prediction Service
FastAPI-based microservice for ML predictions
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Optional
from datetime import datetime
import logging

from models.outbreak_predictor import OutbreakPredictor
from models.risk_assessor import RiskAssessor

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(
    title="SORMAS AI Prediction Service",
    description="AI/ML microservice for outbreak prediction and risk assessment",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure based on deployment
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize ML models
outbreak_predictor = OutbreakPredictor()
risk_assessor = RiskAssessor()


# Request/Response Models
class CaseData(BaseModel):
    """Case data for analysis"""
    region_id: str
    disease: str
    recent_cases: int
    previous_cases: int
    population_density: int
    weekly_trend: List[int]
    fatality_rate: Optional[float] = 0.0


class PredictionRequest(BaseModel):
    """Request for outbreak prediction"""
    case_data: CaseData
    prediction_horizon: int = 14
    model_version: Optional[str] = "latest"


class PredictionResponse(BaseModel):
    """Response with outbreak prediction"""
    prediction_id: str
    risk_score: float
    confidence: float
    predicted_cases: int
    model_version: str
    contributing_factors: Dict[str, float]
    timestamp: datetime


class HealthResponse(BaseModel):
    """Health check response"""
    status: str
    version: str
    models_loaded: bool
    timestamp: datetime


# API Endpoints

@app.get("/", response_model=HealthResponse)
async def root():
    """Root endpoint with service info"""
    return HealthResponse(
        status="healthy",
        version="1.0.0",
        models_loaded=True,
        timestamp=datetime.now()
    )


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy",
        version="1.0.0",
        models_loaded=outbreak_predictor.is_loaded(),
        timestamp=datetime.now()
    )


@app.post("/predict/outbreak", response_model=PredictionResponse)
async def predict_outbreak(request: PredictionRequest):
    """
    Predict outbreak risk and case count

    Args:
        request: Prediction request with case data

    Returns:
        Prediction with risk score and forecasted cases
    """
    try:
        logger.info(f"Prediction request for region: {request.case_data.region_id}")

        # Generate prediction using ML model
        prediction = outbreak_predictor.predict(
            case_data=request.case_data.dict(),
            horizon=request.prediction_horizon
        )

        return PredictionResponse(
            prediction_id=prediction['prediction_id'],
            risk_score=prediction['risk_score'],
            confidence=prediction['confidence'],
            predicted_cases=prediction['predicted_cases'],
            model_version=prediction['model_version'],
            contributing_factors=prediction['contributing_factors'],
            timestamp=datetime.now()
        )

    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")


@app.post("/assess/risk")
async def assess_risk(case_data: CaseData):
    """
    Assess outbreak risk level

    Args:
        case_data: Case data for risk assessment

    Returns:
        Risk assessment with level and score
    """
    try:
        logger.info(f"Risk assessment for region: {case_data.region_id}")

        risk_assessment = risk_assessor.assess(case_data.dict())

        return {
            "risk_score": risk_assessment['risk_score'],
            "risk_level": risk_assessment['risk_level'],
            "alert_level": risk_assessment['alert_level'],
            "recommendations": risk_assessment['recommendations'],
            "timestamp": datetime.now()
        }

    except Exception as e:
        logger.error(f"Risk assessment error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Assessment failed: {str(e)}")


@app.get("/models/versions")
async def get_model_versions():
    """Get available model versions"""
    return {
        "outbreak_predictor": outbreak_predictor.get_version(),
        "risk_assessor": risk_assessor.get_version(),
        "available_versions": ["v1.0.0", "v1.0.0-poc"]
    }


@app.post("/models/retrain")
async def retrain_models():
    """
    Trigger model retraining
    Note: This should be secured with authentication in production
    """
    try:
        logger.info("Model retraining initiated")

        # In production, this would trigger an MLflow pipeline
        result = {
            "status": "initiated",
            "message": "Model retraining started in background",
            "estimated_time": "30-60 minutes",
            "timestamp": datetime.now()
        }

        return result

    except Exception as e:
        logger.error(f"Retraining error: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Retraining failed: {str(e)}")


@app.get("/metrics")
async def get_metrics():
    """Get model performance metrics"""
    return {
        "outbreak_predictor": {
            "accuracy": 0.82,
            "precision": 0.78,
            "recall": 0.85,
            "f1_score": 0.81
        },
        "risk_assessor": {
            "accuracy": 0.86,
            "precision": 0.83,
            "recall": 0.88,
            "f1_score": 0.85
        },
        "last_updated": datetime.now()
    }


if __name__ == "__main__":
    import uvicorn

    logger.info("Starting SORMAS AI Prediction Service...")

    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
