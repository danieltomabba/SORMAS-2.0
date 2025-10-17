# SORMAS AI Implementation Guide

## Overview

This document describes the AI-powered enhancements to SORMAS (Surveillance Outbreak Response Management and Analysis System). The implementation includes:

- **AI Outbreak Prediction**: Machine learning models for predicting disease outbreaks
- **Risk Assessment**: Intelligent risk scoring and classification
- **Modern UI**: Vaadin Flow-based dashboard for AI insights
- **Microservices Architecture**: Python FastAPI service for ML operations
- **MLOps Infrastructure**: MLflow for model management and versioning

## Project Structure

```
sormasAI/
├── sormas-ai-api/              # AI service contracts and DTOs
│   └── src/main/java/de/symeda/sormas/ai/api/
│       ├── AIOutbreakPredictionFacade.java
│       └── dto/
│           ├── OutbreakPredictionDto.java
│           └── AIAnalysisRequestDto.java
│
├── sormas-ai-backend/          # AI backend implementation (Java EJB)
│   └── src/main/java/de/symeda/sormas/ai/backend/
│       ├── AIOutbreakPredictionFacadeEjb.java
│       └── AIModelService.java
│
├── sormas-ai-models/           # Python ML service
│   ├── requirements.txt
│   ├── Dockerfile
│   ├── .env.example
│   └── src/main/python/
│       ├── app.py              # FastAPI application
│       └── models/
│           ├── outbreak_predictor.py
│           └── risk_assessor.py
│
├── sormas-flow/                # Modern Vaadin Flow UI
│   ├── pom.xml
│   ├── package.json
│   └── src/main/java/de/symeda/sormas/flow/
│       └── views/ai/
│           └── AIOutbreakDashboard.java
│
└── docker-compose.yml          # Docker services configuration
```

## Prerequisites

### System Requirements
- **Java**: JDK 11 or higher
- **Maven**: 3.6.3 or higher
- **Node.js**: 18.x or higher
- **Python**: 3.11 or higher
- **Docker & Docker Compose**: Latest version (for containerized deployment)
- **PostgreSQL**: 15 or higher

### Development Tools
- IntelliJ IDEA or Eclipse (for Java development)
- VS Code or PyCharm (for Python development)
- Postman or similar (for API testing)

## Quick Start

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   cd /Users/dtomabba/Desktop/sormasAI
   ```

2. **Configure environment**
   ```bash
   cd sormas-ai-models
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start services**
   ```bash
   cd ..
   docker-compose up -d
   ```

4. **Verify services**
   - AI Service: http://localhost:8000
   - MLflow UI: http://localhost:5000
   - PostgreSQL: localhost:5432

### Option 2: Manual Setup

#### 1. Start PostgreSQL
```bash
# Using Docker
docker run -d \
  --name sormas-postgres \
  -e POSTGRES_DB=sormas \
  -e POSTGRES_USER=sormas \
  -e POSTGRES_PASSWORD=sormas \
  -p 5432:5432 \
  postgres:15-alpine
```

#### 2. Set up Python AI Service
```bash
cd sormas-ai-models

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Set environment variables
export DATABASE_URL="postgresql://sormas:sormas@localhost:5432/sormas"
export MLFLOW_TRACKING_URI="http://localhost:5000"

# Start the service
python src/main/python/app.py
```

The AI service will be available at http://localhost:8000

#### 3. Start MLflow
```bash
# In a new terminal
mlflow server \
  --backend-store-uri postgresql://sormas:sormas@localhost:5432/mlflow \
  --default-artifact-root ./mlflow/artifacts \
  --host 0.0.0.0 \
  --port 5000
```

#### 4. Build SORMAS modules
```bash
# From the SORMAS base directory
cd /Users/dtomabba/Desktop/SORMAS-2.0

# Add AI modules to main POM (if not already present)
# Then build
mvn clean install -DskipTests
```

#### 5. Build and Deploy Flow module
```bash
cd /Users/dtomabba/Desktop/sormasAI/sormas-flow

# Install frontend dependencies
npm install

# Build
mvn clean package

# Deploy to Payara (adjust path as needed)
cp target/sormas-flow.war $PAYARA_HOME/glassfish/domains/domain1/autodeploy/
```

## API Documentation

### AI Service Endpoints

#### Health Check
```bash
GET http://localhost:8000/health
```

#### Predict Outbreak
```bash
POST http://localhost:8000/predict/outbreak
Content-Type: application/json

{
  "case_data": {
    "region_id": "REGION-UUID",
    "disease": "CORONAVIRUS",
    "recent_cases": 150,
    "previous_cases": 100,
    "population_density": 1500,
    "weekly_trend": [80, 95, 120, 150],
    "fatality_rate": 0.02
  },
  "prediction_horizon": 14,
  "model_version": "v1.0.0"
}
```

**Response:**
```json
{
  "prediction_id": "uuid",
  "risk_score": 0.65,
  "confidence": 0.82,
  "predicted_cases": 185,
  "model_version": "v1.0.0-poc",
  "contributing_factors": {
    "Recent Case Volume": 0.35,
    "Growth Rate": 0.25,
    "Population Density": 0.20,
    "Trend Acceleration": 0.15
  },
  "timestamp": "2025-10-17T10:30:00Z"
}
```

#### Assess Risk
```bash
POST http://localhost:8000/assess/risk
Content-Type: application/json

{
  "region_id": "REGION-UUID",
  "disease": "CORONAVIRUS",
  "recent_cases": 150,
  "previous_cases": 100,
  "population_density": 1500
}
```

#### Get Model Versions
```bash
GET http://localhost:8000/models/versions
```

#### Get Metrics
```bash
GET http://localhost:8000/metrics
```

### Java EJB Facade

```java
@Inject
private AIOutbreakPredictionFacade aiPredictionFacade;

// Get prediction for a region
OutbreakPredictionDto prediction = aiPredictionFacade.predictOutbreak(
    regionRef,
    Disease.CORONAVIRUS,
    14 // days ahead
);

// Get all high-risk predictions
List<OutbreakPredictionDto> highRiskPredictions =
    aiPredictionFacade.getPredictionsByRiskLevel(
        RiskLevel.HIGH,
        Disease.CORONAVIRUS
    );

// Perform comprehensive analysis
AIAnalysisRequestDto request = new AIAnalysisRequestDto();
request.setAnalysisType(AnalysisType.OUTBREAK_PREDICTION);
request.setRegions(regions);
request.setDiseases(diseases);
request.setPredictionHorizon(14);

List<OutbreakPredictionDto> results =
    aiPredictionFacade.performAnalysis(request);
```

## Accessing the AI Dashboard

1. Start your SORMAS application
2. Navigate to: `http://localhost:6080/sormas-flow/ai/outbreak-dashboard`
3. Select a disease from the dropdown
4. View AI predictions, risk scores, and recommendations
5. Click "Refresh" to update predictions
6. Click "Retrain Models" to trigger model retraining

## Configuration

### Application Properties

#### Java Backend (`sormas.properties`)
```properties
# AI Service Configuration
ai.service.url=http://localhost:8000
ai.service.timeout=30000

# Feature Flags
ai.outbreak.prediction.enabled=true
ai.risk.assessment.enabled=true
```

#### Python Service (`.env`)
```env
DATABASE_URL=postgresql://sormas:sormas@localhost:5432/sormas
MLFLOW_TRACKING_URI=http://localhost:5000
MODEL_PATH=./models/trained
MODEL_VERSION=v1.0.0
LOG_LEVEL=INFO
API_HOST=0.0.0.0
API_PORT=8000
```

## Model Training

### Preparing Training Data

1. **Export case data from SORMAS**
   ```sql
   -- Example SQL to extract training data
   SELECT
       r.name as region,
       c.disease,
       c.report_date,
       COUNT(*) as case_count
   FROM cases c
   JOIN regions r ON c.region_id = r.id
   WHERE c.report_date >= NOW() - INTERVAL '2 years'
   GROUP BY r.name, c.disease, c.report_date
   ORDER BY c.report_date;
   ```

2. **Save to CSV**
   ```bash
   psql -h localhost -U sormas -d sormas \
     -c "COPY (...query...) TO STDOUT WITH CSV HEADER" \
     > training_data.csv
   ```

### Training Models

```python
# In Python environment
from models.outbreak_predictor import OutbreakPredictor
from models.model_trainer import ModelTrainer

# Initialize trainer
trainer = ModelTrainer()

# Load data
data = trainer.load_training_data('training_data.csv')

# Train model
model = trainer.train_outbreak_predictor(data)

# Evaluate
metrics = trainer.evaluate(model, test_data)
print(f"Accuracy: {metrics['accuracy']}")

# Save model
trainer.save_model(model, 'models/trained/outbreak_predictor_v2.pkl')

# Register in MLflow
mlflow.sklearn.log_model(model, "outbreak-predictor")
```

## Testing

### Unit Tests (Java)
```bash
cd sormas-ai-backend
mvn test
```

### Integration Tests (Python)
```bash
cd sormas-ai-models
pytest tests/
```

### API Testing with curl
```bash
# Test health endpoint
curl http://localhost:8000/health

# Test prediction endpoint
curl -X POST http://localhost:8000/predict/outbreak \
  -H "Content-Type: application/json" \
  -d @test_data/sample_prediction_request.json
```

## Deployment

### Production Deployment

1. **Update configuration for production**
   - Set proper database credentials
   - Configure API authentication
   - Set up HTTPS/TLS
   - Enable monitoring

2. **Build production images**
   ```bash
   docker-compose -f docker-compose.prod.yml build
   ```

3. **Deploy**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

4. **Set up monitoring**
   - Configure Prometheus for metrics
   - Set up Grafana dashboards
   - Enable application logging

## Monitoring

### Health Checks
```bash
# AI Service health
curl http://localhost:8000/health

# Check model metrics
curl http://localhost:8000/metrics
```

### MLflow UI
- Access: http://localhost:5000
- View experiments, models, and metrics
- Compare model versions
- Track model performance over time

## Troubleshooting

### Common Issues

**Issue: AI service not starting**
```bash
# Check logs
docker logs sormas-ai-service

# Verify Python dependencies
pip list

# Check database connection
psql -h localhost -U sormas -d sormas -c "SELECT 1"
```

**Issue: Predictions failing**
```bash
# Check AI service logs
tail -f sormas-ai-models/logs/app.log

# Verify model files
ls -la sormas-ai-models/models/trained/

# Test endpoint directly
curl http://localhost:8000/predict/outbreak -d '{...}'
```

**Issue: Flow UI not loading**
```bash
# Rebuild frontend
cd sormas-flow
npm install
mvn clean package

# Check Payara logs
tail -f $PAYARA_HOME/glassfish/domains/domain1/logs/server.log
```

## Next Steps

1. **Enhance ML Models**
   - Implement LSTM for time-series forecasting
   - Add Prophet for seasonal predictions
   - Integrate external data sources (weather, mobility)

2. **Add More AI Features**
   - Natural Language Processing for case descriptions
   - Contact tracing optimization
   - Resource allocation recommendations
   - Automated report generation

3. **Improve UI**
   - Add interactive charts (Highcharts/Chart.js)
   - Real-time updates with WebSockets
   - Export capabilities for predictions
   - Mobile-responsive design

4. **Security & Authentication**
   - Add JWT authentication for AI API
   - Implement role-based access control
   - Secure model endpoints
   - Audit logging

5. **Performance Optimization**
   - Add Redis caching for predictions
   - Implement model serving with TensorFlow Serving
   - Optimize database queries
   - Add CDN for static assets

## Contributing

Please follow the SORMAS contribution guidelines and ensure all tests pass before submitting pull requests.

## Support

For issues or questions:
- GitHub Issues: https://github.com/danieltomabba/SORMAS-2.0/issues
- Documentation: https://sormas.org

## License

This project follows the same GPL-3.0 license as SORMAS.

---

**Generated with AI-powered SORMAS Implementation**
Version: 1.0.0-POC
Date: 2025-10-17
