# Project Requirements Document (PRD)
# AI-Enhanced SORMAS: Outbreak Prediction System
## Part 4: Complete Implementation Guide

---

## Document Information

| Field | Value |
|-------|-------|
| **Document** | PRD Part 4 of 4 (Consolidated) |
| **Version** | 1.0.0 |
| **Date** | October 17, 2025 |
| **Purpose** | Complete implementation reference for Lovable.dev |
| **Dependencies** | PRD_01, PRD_02, PRD_03 |

---

## Table of Contents

1. [API Contracts and Integration](#1-api-contracts-and-integration)
2. [UI/UX Requirements](#2-uiux-requirements)
3. [Infrastructure and Deployment](#3-infrastructure-and-deployment)
4. [Data Models and Schemas](#4-data-models-and-schemas)
5. [Testing Requirements](#5-testing-requirements)
6. [Complete File Structure](#6-complete-file-structure)
7. [Step-by-Step Implementation Guide](#7-step-by-step-implementation-guide)
8. [Verification Checklist](#8-verification-checklist)

---

## 1. API Contracts and Integration

### 1.1 Python ML Service API (FastAPI)

#### Endpoint Specifications

##### POST /predict/outbreak

**Request Schema:**
```json
{
  "case_data": {
    "region_id": "string (UUID)",
    "disease": "string (ENUM)",
    "recent_cases": "integer",
    "previous_cases": "integer",
    "population_density": "integer",
    "weekly_trend": ["integer array"],
    "fatality_rate": "float (0.0-1.0)"
  },
  "prediction_horizon": "integer (7, 14, or 30)",
  "model_version": "string (optional)"
}
```

**Response Schema:**
```json
{
  "prediction_id": "string (UUID)",
  "risk_score": "float (0.0-1.0)",
  "confidence": "float (0.0-1.0)",
  "predicted_cases": "integer",
  "risk_level": "LOW | MODERATE | HIGH | CRITICAL",
  "alert_level": "NONE | WATCH | WARNING | EMERGENCY",
  "model_version": "string",
  "contributing_factors": {
    "Recent Case Volume": "float",
    "Growth Rate": "float",
    "Population Density": "float",
    "Trend Acceleration": "float"
  },
  "timestamp": "string (ISO 8601)"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/predict/outbreak \
  -H "Content-Type: application/json" \
  -d '{
    "case_data": {
      "region_id": "REGION-UUID-123",
      "disease": "CORONAVIRUS",
      "recent_cases": 150,
      "previous_cases": 100,
      "population_density": 1500,
      "weekly_trend": [80, 95, 120, 150],
      "fatality_rate": 0.02
    },
    "prediction_horizon": 14
  }'
```

##### POST /assess/risk

**Request Schema:**
```json
{
  "region_id": "string",
  "disease": "string",
  "recent_cases": "integer",
  "previous_cases": "integer",
  "population_density": "integer"
}
```

**Response Schema:**
```json
{
  "region_id": "string",
  "disease": "string",
  "risk_score": "float",
  "risk_level": "string",
  "alert_level": "string",
  "recommendations": "string array"
}
```

##### GET /models/versions

**Response Schema:**
```json
{
  "versions": [
    {
      "version": "v1.0.0-poc",
      "created_at": "2025-10-17T10:00:00Z",
      "accuracy": 0.75,
      "status": "active"
    }
  ]
}
```

##### POST /models/retrain

**Request Schema:**
```json
{
  "start_date": "string (ISO 8601, optional)",
  "end_date": "string (ISO 8601, optional)",
  "diseases": ["string array, optional"]
}
```

**Response Schema:**
```json
{
  "job_id": "string",
  "status": "initiated",
  "estimated_time": "5 minutes"
}
```

##### GET /health

**Response Schema:**
```json
{
  "status": "healthy",
  "service": "SORMAS AI Service",
  "version": "v1.0.0-poc"
}
```

### 1.2 Java EJB to Python Communication

**HTTP Client Pattern:**
```java
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AIModelService {

    private static final String AI_SERVICE_URL =
        System.getProperty("ai.service.url", "http://localhost:8000");
    private static final MediaType JSON =
        MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PredictionResponse requestPrediction(
            RegionReferenceDto region,
            Disease disease,
            CaseDataSummary caseData,
            int population,
            int horizon) throws IOException {

        // Build request payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("region_id", region.getUuid());
        caseDataMap.put("disease", disease.name());
        caseDataMap.put("recent_cases", caseData.getRecentCases());
        caseDataMap.put("previous_cases", caseData.getPreviousCases());
        caseDataMap.put("population_density", population / caseData.getArea());
        caseDataMap.put("weekly_trend", caseData.getWeeklyTrend());
        caseDataMap.put("fatality_rate", caseData.getFatalityRate());

        payload.put("case_data", caseDataMap);
        payload.put("prediction_horizon", horizon);

        String json = objectMapper.writeValueAsString(payload);

        // Make HTTP request
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
            .url(AI_SERVICE_URL + "/predict/outbreak")
            .post(body)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, PredictionResponse.class);
        }
    }

    public static class PredictionResponse {
        public String prediction_id;
        public double risk_score;
        public double confidence;
        public int predicted_cases;
        public String risk_level;
        public String alert_level;
        public String model_version;
        public Map<String, Double> contributing_factors;
        public String timestamp;
    }
}
```

### 1.3 Integration Testing

**Test Scenario 1: Successful Prediction**
```java
@Test
public void testOutbreakPrediction() {
    RegionReferenceDto region = new RegionReferenceDto("uuid-123", "Test Region");
    Disease disease = Disease.CORONAVIRUS;

    OutbreakPredictionDto prediction =
        aiPredictionFacade.predictOutbreak(region, disease, 14);

    assertNotNull(prediction);
    assertNotNull(prediction.getPredictionId());
    assertTrue(prediction.getRiskScore() >= 0.0 && prediction.getRiskScore() <= 1.0);
    assertTrue(prediction.getConfidence() >= 0.0 && prediction.getConfidence() <= 1.0);
    assertNotNull(prediction.getRiskLevel());
    assertNotNull(prediction.getAlertLevel());
}
```

**Test Scenario 2: Service Unavailable Fallback**
```java
@Test
public void testFallbackWhenServiceUnavailable() {
    // Simulate ML service down
    // Should fall back to POC algorithm

    OutbreakPredictionDto prediction =
        aiPredictionFacade.predictOutbreak(region, disease, 14);

    assertEquals("v1.0.0-poc", prediction.getModelVersion());
    // Should still return valid prediction
}
```

---

## 2. UI/UX Requirements

### 2.1 AI Outbreak Dashboard (Vaadin Flow)

#### Layout Structure

```
┌─────────────────────────────────────────────────────────────┐
│  Header                                                      │
│  ┌─────────────────────┐  ┌────────┐  ┌────────┐          │
│  │ AI Outbreak         │  │Refresh │  │Retrain │          │
│  │ Predictions         │  │        │  │Models  │          │
│  └─────────────────────┘  └────────┘  └────────┘          │
├─────────────────────────────────────────────────────────────┤
│  Filters                                                     │
│  ┌──────────────────────────────────┐                       │
│  │ Disease: [Dropdown          ▼]   │                       │
│  └──────────────────────────────────┘                       │
├─────────────────────────────────────────────────────────────┤
│  Summary Cards (4 columns)                                  │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐          │
│  │Critical│  │  High  │  │Moderate│  │Predicted│          │
│  │Risk: 2 │  │Risk: 5 │  │Risk: 8 │  │Cases:  │          │
│  │        │  │        │  │        │  │1,250   │          │
│  └────────┘  └────────┘  └────────┘  └────────┘          │
├─────────────────────────────────────────────────────────────┤
│  Prediction Cards (scrollable list)                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Region: Central Region          [CRITICAL] 🔴      │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │ Risk Score: 85%  │  Predicted: 450  │  Confidence: │   │
│  │                  │  cases           │  82%         │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │ Recommendations:                                     │   │
│  │ • Activate emergency response protocols             │   │
│  │ • Deploy rapid response teams                       │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ Region: Northern Region         [HIGH] 🟠          │   │
│  │ ...                                                  │   │
└─────────────────────────────────────────────────────────────┘
```

#### Component Specifications

**1. Header Component**
```java
private Component createHeader() {
    H2 title = new H2("AI-Powered Outbreak Predictions");
    title.addClassName("dashboard-title");

    Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
    refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshButton.addClickListener(e -> refreshData());

    Button retrainButton = new Button("Retrain Models", VaadinIcon.COG.create());
    retrainButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
    retrainButton.addClickListener(e -> triggerRetraining());

    HorizontalLayout header = new HorizontalLayout(title, refreshButton, retrainButton);
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);
    header.setAlignItems(Alignment.CENTER);
    header.addClassName("dashboard-header");

    return header;
}
```

**2. Disease Filter Component**
```java
private Component createFilters() {
    diseaseFilter = new ComboBox<>("Disease");
    diseaseFilter.setItems(Disease.values());
    diseaseFilter.setItemLabelGenerator(Disease::toString);
    diseaseFilter.setPlaceholder("Select disease...");
    diseaseFilter.setWidthFull();
    diseaseFilter.setClearButtonVisible(true);
    diseaseFilter.addValueChangeListener(e -> loadPredictions(e.getValue()));

    HorizontalLayout filters = new HorizontalLayout(diseaseFilter);
    filters.setWidthFull();
    filters.addClassName("dashboard-filters");

    return filters;
}
```

**3. Summary Card Component**
```java
private Component createSummaryCard(String title, String value, String theme) {
    Div card = new Div();
    card.addClassName("summary-card");
    card.addClassName("summary-card-" + theme);

    H3 cardTitle = new H3(title);
    cardTitle.addClassName("summary-card-title");

    Span cardValue = new Span(value);
    cardValue.addClassName("summary-card-value");

    card.add(cardTitle, cardValue);
    return card;
}
```

**4. Prediction Card Component**
```java
private Component createPredictionCard(OutbreakPredictionDto prediction) {
    Div card = new Div();
    card.addClassName("prediction-card");
    card.addClassName("prediction-card-" +
        prediction.getRiskLevel().name().toLowerCase());

    // Header with region name and risk badge
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    H3 regionTitle = new H3(prediction.getRegion().getCaption());
    regionTitle.addClassName("prediction-region");

    Span riskBadge = new Span(prediction.getRiskLevel().toString());
    riskBadge.getElement().getThemeList().add("badge");
    riskBadge.getElement().getThemeList().add(
        getRiskLevelTheme(prediction.getRiskLevel())
    );

    header.add(regionTitle, riskBadge);

    // Metrics grid
    HorizontalLayout metrics = new HorizontalLayout();
    metrics.setWidthFull();
    metrics.addClassName("prediction-metrics");

    metrics.add(
        createMetric("Risk Score",
            String.format("%.0f%%", prediction.getRiskScore() * 100)),
        createMetric("Predicted Cases",
            String.valueOf(prediction.getPredictedCases())),
        createMetric("Confidence",
            String.format("%.0f%%", prediction.getConfidence() * 100)),
        createMetric("Alert Level",
            prediction.getAlertLevel().toString())
    );

    // Recommendations
    Div recommendations = new Div();
    recommendations.addClassName("prediction-recommendations");

    Span recTitle = new Span("Recommendations:");
    recTitle.addClassName("recommendations-title");

    Span recText = new Span(prediction.getRecommendations());
    recText.addClassName("recommendations-text");

    recommendations.add(recTitle, recText);

    card.add(header, metrics, recommendations);
    return card;
}

private Component createMetric(String label, String value) {
    VerticalLayout metric = new VerticalLayout();
    metric.setPadding(false);
    metric.setSpacing(false);
    metric.addClassName("metric");

    Span labelSpan = new Span(label);
    labelSpan.addClassName("metric-label");

    Span valueSpan = new Span(value);
    valueSpan.addClassName("metric-value");

    metric.add(labelSpan, valueSpan);
    return metric;
}
```

#### CSS Styling (styles.css)

```css
/* Dashboard Styles */
.dashboard-title {
    margin: 0;
    color: var(--lumo-header-text-color);
}

.dashboard-header {
    padding: var(--lumo-space-l);
    background: var(--lumo-contrast-5pct);
    border-bottom: 1px solid var(--lumo-contrast-10pct);
}

.dashboard-filters {
    padding: var(--lumo-space-m);
}

/* Summary Cards */
.summary-card {
    padding: var(--lumo-space-l);
    border-radius: var(--lumo-border-radius-m);
    background: var(--lumo-contrast-5pct);
    border-left: 4px solid;
}

.summary-card-error {
    border-left-color: var(--lumo-error-color);
}

.summary-card-warning {
    border-left-color: var(--lumo-warning-color);
}

.summary-card-info {
    border-left-color: var(--lumo-primary-color);
}

.summary-card-primary {
    border-left-color: var(--lumo-primary-color);
}

.summary-card-title {
    margin: 0 0 var(--lumo-space-s) 0;
    font-size: var(--lumo-font-size-m);
    color: var(--lumo-secondary-text-color);
}

.summary-card-value {
    font-size: var(--lumo-font-size-xxxl);
    font-weight: bold;
    color: var(--lumo-primary-text-color);
}

/* Prediction Cards */
.prediction-card {
    padding: var(--lumo-space-l);
    margin-bottom: var(--lumo-space-m);
    border-radius: var(--lumo-border-radius-m);
    background: var(--lumo-contrast-5pct);
    border-left: 4px solid;
    box-shadow: var(--lumo-box-shadow-xs);
}

.prediction-card-critical {
    border-left-color: var(--lumo-error-color);
}

.prediction-card-high {
    border-left-color: var(--lumo-warning-color);
}

.prediction-card-moderate {
    border-left-color: var(--lumo-primary-color);
}

.prediction-card-low {
    border-left-color: var(--lumo-success-color);
}

.prediction-region {
    margin: 0;
    font-size: var(--lumo-font-size-l);
}

.prediction-metrics {
    margin-top: var(--lumo-space-m);
    gap: var(--lumo-space-l);
}

.metric {
    flex: 1;
}

.metric-label {
    font-size: var(--lumo-font-size-s);
    color: var(--lumo-secondary-text-color);
}

.metric-value {
    font-size: var(--lumo-font-size-xl);
    font-weight: 600;
    color: var(--lumo-primary-text-color);
}

.prediction-recommendations {
    margin-top: var(--lumo-space-m);
    padding: var(--lumo-space-m);
    background: var(--lumo-contrast-10pct);
    border-radius: var(--lumo-border-radius-s);
}

.recommendations-title {
    font-weight: 600;
    display: block;
    margin-bottom: var(--lumo-space-s);
}

.recommendations-text {
    white-space: pre-wrap;
    line-height: 1.6;
}

/* Responsive */
@media (max-width: 768px) {
    .prediction-metrics {
        flex-direction: column;
    }
}
```

#### UI Requirements

**REQ-UI-001**: Dashboard must be accessible at `/sormas-flow/ai/outbreak-dashboard`
**REQ-UI-002**: All text must support internationalization (i18n)
**REQ-UI-003**: Dashboard must be responsive (mobile, tablet, desktop)
**REQ-UI-004**: Color scheme must follow Vaadin Lumo theme
**REQ-UI-005**: Loading indicators must be shown during data fetching
**REQ-UI-006**: Error messages must be user-friendly
**REQ-UI-007**: Refresh button must provide visual feedback
**REQ-UI-008**: Risk levels must use consistent color coding:
  - CRITICAL: Red (#f44336)
  - HIGH: Orange (#ff9800)
  - MODERATE: Blue (#2196f3)
  - LOW: Green (#4caf50)

---

## 3. Infrastructure and Deployment

### 3.1 Docker Configuration

#### Dockerfile (Python ML Service)

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY src/main/python/ ./

# Create directories for models and logs
RUN mkdir -p /app/models/trained /app/logs

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8000/health')"

# Run application
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

#### docker-compose.ai.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: sormas-postgres
    environment:
      POSTGRES_DB: sormas
      POSTGRES_USER: sormas
      POSTGRES_PASSWORD: sormas
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - sormas-ai-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sormas"]
      interval: 10s
      timeout: 5s
      retries: 5

  sormas-ai:
    build:
      context: ./sormas-ai-models
      dockerfile: Dockerfile
    container_name: sormas-ai-service
    environment:
      DATABASE_URL: postgresql://sormas:sormas@postgres:5432/sormas
      MLFLOW_TRACKING_URI: http://mlflow:5000
      MODEL_PATH: /app/models/trained
      MODEL_VERSION: v1.0.0-poc
      LOG_LEVEL: INFO
      API_HOST: 0.0.0.0
      API_PORT: 8000
    ports:
      - "8000:8000"
    volumes:
      - ./sormas-ai-models/models:/app/models
      - ./sormas-ai-models/logs:/app/logs
    depends_on:
      postgres:
        condition: service_healthy
      mlflow:
        condition: service_started
    networks:
      - sormas-ai-network
    restart: unless-stopped

  mlflow:
    image: ghcr.io/mlflow/mlflow:v2.8.1
    container_name: sormas-mlflow
    command: >
      mlflow server
      --backend-store-uri postgresql://sormas:sormas@postgres:5432/mlflow
      --default-artifact-root /mlflow/artifacts
      --host 0.0.0.0
      --port 5000
    environment:
      MLFLOW_BACKEND_STORE_URI: postgresql://sormas:sormas@postgres:5432/mlflow
    ports:
      - "5000:5000"
    volumes:
      - mlflow-data:/mlflow
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - sormas-ai-network
    restart: unless-stopped

  redis:
    image: redis:7.2-alpine
    container_name: sormas-redis
    command: redis-server --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - sormas-ai-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3

volumes:
  postgres-data:
    driver: local
  mlflow-data:
    driver: local
  redis-data:
    driver: local

networks:
  sormas-ai-network:
    driver: bridge
```

#### Environment Configuration

**.env.example**:
```bash
# Database
DATABASE_URL=postgresql://sormas:sormas@localhost:5432/sormas

# MLflow
MLFLOW_TRACKING_URI=http://localhost:5000

# ML Models
MODEL_PATH=/app/models/trained
MODEL_VERSION=v1.0.0-poc

# API Configuration
API_HOST=0.0.0.0
API_PORT=8000
DEBUG=false

# Logging
LOG_LEVEL=INFO

# Security (Future)
# API_KEY=your-secret-api-key
# JWT_SECRET=your-jwt-secret
```

### 3.2 Startup Script

**start-ai-services.sh**:
```bash
#!/bin/bash

set -e

echo "==================================="
echo "SORMAS AI Services Startup"
echo "==================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running${NC}"
    exit 1
fi

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Using 'docker compose' instead of 'docker-compose'${NC}"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Check service health
check_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}Waiting for $service_name...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -f $url > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is ready!${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo -e "${RED}✗ $service_name failed to start${NC}"
    return 1
}

# Start services
echo "Starting Docker services..."
$DOCKER_COMPOSE -f docker-compose.ai.yml up -d

echo ""
echo "Services starting..."
echo ""

# Wait for services
check_service "PostgreSQL" "localhost:5432" || true
check_service "AI Service" "http://localhost:8000/health"
check_service "MLflow" "http://localhost:5000"
check_service "Redis" "localhost:6379" || true

echo ""
echo "==================================="
echo -e "${GREEN}All services are running!${NC}"
echo "==================================="
echo ""
echo "Service URLs:"
echo "  - AI Service API: http://localhost:8000"
echo "  - AI Service Docs: http://localhost:8000/docs"
echo "  - MLflow UI: http://localhost:5000"
echo "  - PostgreSQL: localhost:5432"
echo ""
echo "To view logs:"
echo "  $DOCKER_COMPOSE -f docker-compose.ai.yml logs -f sormas-ai"
echo ""
echo "To stop services:"
echo "  $DOCKER_COMPOSE -f docker-compose.ai.yml down"
echo ""
echo "==================================="
```

### 3.3 Deployment Steps

**Step 1: Clone Repository**
```bash
git clone https://github.com/danieltomabba/SORMAS-2.0.git
cd SORMAS-2.0
git checkout feature/ai-outbreak-prediction
```

**Step 2: Configure Environment**
```bash
cd sormas-ai-models
cp .env.example .env
# Edit .env with appropriate values
```

**Step 3: Start AI Services**
```bash
cd ..
chmod +x start-ai-services.sh
./start-ai-services.sh
```

**Step 4: Build Java Modules**
```bash
cd sormas-base
mvn clean install -DskipTests
```

**Step 5: Deploy to Payara**
```bash
# Copy EAR
cp sormas-ear/target/sormas-ear.ear \
   $PAYARA_HOME/glassfish/domains/domain1/autodeploy/

# Copy Flow WAR (optional, if standalone)
cp ../sormas-flow/target/sormas-flow.war \
   $PAYARA_HOME/glassfish/domains/domain1/autodeploy/
```

**Step 6: Verify Deployment**
```bash
# Check AI Service
curl http://localhost:8000/health

# Check MLflow
curl http://localhost:5000

# Check Dashboard (after Payara starts)
curl http://localhost:6080/sormas-flow/ai/outbreak-dashboard
```

---

## 4. Data Models and Schemas

### 4.1 Database Schema (Future - Not POC)

```sql
-- Create AI schema
CREATE SCHEMA IF NOT EXISTS ai_predictions;

-- Outbreak Predictions Table
CREATE TABLE ai_predictions.outbreak_predictions (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    prediction_id VARCHAR(255) NOT NULL UNIQUE,

    -- References
    region_id BIGINT NOT NULL REFERENCES region(id),
    disease VARCHAR(255) NOT NULL,

    -- Risk Assessment
    risk_score DECIMAL(5, 4) NOT NULL CHECK (risk_score >= 0 AND risk_score <= 1),
    confidence DECIMAL(5, 4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    risk_level VARCHAR(50) NOT NULL CHECK (risk_level IN ('LOW', 'MODERATE', 'HIGH', 'CRITICAL')),
    alert_level VARCHAR(50) NOT NULL CHECK (alert_level IN ('NONE', 'WATCH', 'WARNING', 'EMERGENCY')),

    -- Predictions
    predicted_cases INTEGER NOT NULL,
    current_cases INTEGER,
    prediction_horizon INTEGER NOT NULL,
    growth_rate DECIMAL(10, 6),

    -- Analysis
    contributing_factors JSONB,
    recommendations TEXT,

    -- Metadata
    model_version VARCHAR(100) NOT NULL,
    prediction_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMP,

    -- Audit
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_predictions_region ON ai_predictions.outbreak_predictions(region_id);
CREATE INDEX idx_predictions_disease ON ai_predictions.outbreak_predictions(disease);
CREATE INDEX idx_predictions_timestamp ON ai_predictions.outbreak_predictions(prediction_timestamp);
CREATE INDEX idx_predictions_risk_level ON ai_predictions.outbreak_predictions(risk_level);

-- Model Versions Table
CREATE TABLE ai_predictions.model_versions (
    id BIGSERIAL PRIMARY KEY,
    version VARCHAR(100) NOT NULL UNIQUE,
    model_type VARCHAR(100) NOT NULL,

    -- Metrics
    accuracy DECIMAL(5, 4),
    precision_score DECIMAL(5, 4),
    recall DECIMAL(5, 4),
    f1_score DECIMAL(5, 4),
    mae DECIMAL(10, 2),
    mape DECIMAL(10, 2),

    -- Metadata
    training_data_start DATE,
    training_data_end DATE,
    training_duration_seconds INTEGER,
    mlflow_run_id VARCHAR(255),

    status VARCHAR(50) NOT NULL DEFAULT 'training'
        CHECK (status IN ('training', 'active', 'deprecated', 'failed')),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deployed_at TIMESTAMP,
    deprecated_at TIMESTAMP
);

-- Prediction History (for analytics)
CREATE TABLE ai_predictions.prediction_history (
    id BIGSERIAL PRIMARY KEY,
    prediction_id VARCHAR(255) NOT NULL,
    region_id BIGINT NOT NULL,
    disease VARCHAR(255) NOT NULL,
    predicted_cases INTEGER NOT NULL,
    actual_cases INTEGER,
    prediction_date DATE NOT NULL,
    target_date DATE NOT NULL,
    model_version VARCHAR(100) NOT NULL,
    accuracy_score DECIMAL(5, 4),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_history_prediction ON ai_predictions.prediction_history(prediction_id);
CREATE INDEX idx_history_target_date ON ai_predictions.prediction_history(target_date);
```

### 4.2 Java Entity (Future)

```java
package de.symeda.sormas.ai.backend.model;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "outbreak_predictions", schema = "ai_predictions")
public class OutbreakPrediction extends AbstractDomainObject {

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Disease disease;

    @Column(name = "prediction_id", nullable = false, unique = true)
    private String predictionId;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", nullable = false)
    private AlertLevel alertLevel;

    @Column(name = "predicted_cases", nullable = false)
    private Integer predictedCases;

    @Column(name = "current_cases")
    private Integer currentCases;

    @Column(name = "prediction_horizon", nullable = false)
    private Integer predictionHorizon;

    @Column(name = "growth_rate")
    private Double growthRate;

    @Column(name = "contributing_factors", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Double> contributingFactors;

    @Column(name = "recommendations", columnDefinition = "text")
    private String recommendations;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "prediction_timestamp", nullable = false)
    private Date predictionTimestamp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_until")
    private Date validUntil;

    // Getters and setters
}
```

---

## 5. Testing Requirements

### 5.1 Unit Tests

#### Java Backend Tests

```java
@RunWith(MockitoJUnitRunner.class)
public class AIOutbreakPredictionFacadeEjbTest {

    @InjectMocks
    private AIOutbreakPredictionFacadeEjb facade;

    @Mock
    private CaseFacadeEjb caseFacade;

    @Mock
    private RegionFacade regionFacade;

    @Mock
    private AIModelService aiModelService;

    @Test
    public void testPredictOutbreak_Success() {
        // Arrange
        RegionReferenceDto region = new RegionReferenceDto("uuid", "Test Region");
        Disease disease = Disease.CORONAVIRUS;
        Integer horizon = 14;

        CaseDataSummary mockSummary = new CaseDataSummary();
        mockSummary.setRecentCases(100);
        mockSummary.setGrowthRate(0.15);

        when(caseDataAnalysisService.analyzeCaseData(region, disease))
            .thenReturn(mockSummary);

        // Act
        OutbreakPredictionDto result = facade.predictOutbreak(region, disease, horizon);

        // Assert
        assertNotNull(result);
        assertEquals(region, result.getRegion());
        assertEquals(disease, result.getDisease());
        assertTrue(result.getRiskScore() >= 0.0 && result.getRiskScore() <= 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredictOutbreak_InvalidHorizon() {
        RegionReferenceDto region = new RegionReferenceDto("uuid", "Test Region");
        Disease disease = Disease.CORONAVIRUS;
        Integer invalidHorizon = 100; // > 90 days

        facade.predictOutbreak(region, disease, invalidHorizon);
    }

    @Test
    public void testPredictOutbreak_MLServiceFallback() {
        // Arrange
        when(aiModelService.requestPrediction(any(), any(), any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        OutbreakPredictionDto result = facade.predictOutbreak(region, disease, 14);

        // Assert
        assertEquals("v1.0.0-poc", result.getModelVersion());
        // Should use POC algorithm
    }
}
```

#### Python ML Service Tests

```python
import pytest
from fastapi.testclient import TestClient
from app import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_predict_outbreak_success():
    payload = {
        "case_data": {
            "region_id": "test-region-uuid",
            "disease": "CORONAVIRUS",
            "recent_cases": 150,
            "previous_cases": 100,
            "population_density": 1500,
            "weekly_trend": [80, 95, 120, 150],
            "fatality_rate": 0.02
        },
        "prediction_horizon": 14
    }

    response = client.post("/predict/outbreak", json=payload)
    assert response.status_code == 200

    data = response.json()
    assert "prediction_id" in data
    assert "risk_score" in data
    assert 0.0 <= data["risk_score"] <= 1.0
    assert data["risk_level"] in ["LOW", "MODERATE", "HIGH", "CRITICAL"]

def test_predict_outbreak_invalid_horizon():
    payload = {
        "case_data": {...},
        "prediction_horizon": 100  # Invalid
    }

    response = client.post("/predict/outbreak", json=payload)
    assert response.status_code == 422  # Validation error

def test_assess_risk_success():
    payload = {
        "region_id": "test-region",
        "disease": "CORONAVIRUS",
        "recent_cases": 150,
        "previous_cases": 100,
        "population_density": 1500
    }

    response = client.post("/assess/risk", json=payload)
    assert response.status_code == 200

    data = response.json()
    assert "risk_score" in data
    assert "risk_level" in data
```

### 5.2 Integration Tests

```java
@Test
public void testEndToEnd_PredictionWorkflow() {
    // 1. Create test region with cases
    RegionDto region = createTestRegion("Test Region");
    createTestCases(region, Disease.CORONAVIRUS, 100);

    // 2. Request prediction
    OutbreakPredictionDto prediction =
        aiPredictionFacade.predictOutbreak(
            region.toReference(),
            Disease.CORONAVIRUS,
            14
        );

    // 3. Verify prediction
    assertNotNull(prediction);
    assertNotNull(prediction.getPredictionId());
    assertTrue(prediction.getPredictedCases() > 0);

    // 4. Verify in dashboard
    List<OutbreakPredictionDto> dashboardPredictions =
        aiPredictionFacade.getCurrentPredictions(Disease.CORONAVIRUS);

    assertTrue(dashboardPredictions.stream()
        .anyMatch(p -> p.getPredictionId().equals(prediction.getPredictionId())));
}
```

### 5.3 Performance Tests

```python
import time
from locust import HttpUser, task, between

class AIServiceUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def predict_outbreak(self):
        payload = {
            "case_data": {
                "region_id": f"region-{self.environment.runner.user_count}",
                "disease": "CORONAVIRUS",
                "recent_cases": 150,
                "previous_cases": 100,
                "population_density": 1500,
                "weekly_trend": [80, 95, 120, 150],
                "fatality_rate": 0.02
            },
            "prediction_horizon": 14
        }

        with self.client.post(
            "/predict/outbreak",
            json=payload,
            catch_response=True
        ) as response:
            if response.elapsed.total_seconds() > 2.0:
                response.failure(f"Slow response: {response.elapsed.total_seconds()}s")
            elif response.status_code == 200:
                response.success()
```

### 5.4 Test Coverage Requirements

**REQ-TEST-001**: Java backend unit test coverage > 80%
**REQ-TEST-002**: Python service test coverage > 80%
**REQ-TEST-003**: All API endpoints must have integration tests
**REQ-TEST-004**: Performance tests must verify P95 < 2 seconds
**REQ-TEST-005**: Load tests must verify 100 concurrent users

---

## 6. Complete File Structure

```
SORMAS-2.0/
├── sormas-ai-api/
│   ├── pom.xml
│   └── src/main/java/de/symeda/sormas/ai/api/
│       ├── AIOutbreakPredictionFacade.java
│       ├── dto/
│       │   ├── OutbreakPredictionDto.java
│       │   ├── AIAnalysisRequestDto.java
│       │   ├── ModelVersionDto.java
│       │   └── PredictionMetricsDto.java
│       └── enums/
│           ├── RiskLevel.java
│           ├── AlertLevel.java
│           └── AnalysisType.java
│
├── sormas-ai-backend/
│   ├── pom.xml
│   └── src/main/java/de/symeda/sormas/ai/backend/
│       ├── AIOutbreakPredictionFacadeEjb.java
│       ├── AIModelService.java
│       ├── CaseDataAnalysisService.java
│       ├── RiskAssessmentService.java
│       └── util/
│           ├── PredictionCache.java
│           └── AIConfigurationHelper.java
│
├── sormas-ai-models/
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── .env.example
│   ├── README.md
│   └── src/main/python/
│       ├── app.py
│       ├── config.py
│       ├── models/
│       │   ├── __init__.py
│       │   ├── outbreak_predictor.py
│       │   ├── risk_assessor.py
│       │   ├── model_trainer.py
│       │   └── feature_engineering.py
│       ├── api/
│       │   ├── __init__.py
│       │   ├── predict_routes.py
│       │   ├── admin_routes.py
│       │   └── schemas.py
│       └── utils/
│           ├── database.py
│           ├── mlflow_client.py
│           └── logger.py
│
├── sormas-flow/
│   ├── pom.xml
│   ├── package.json
│   └── src/main/java/de/symeda/sormas/flow/
│       └── views/ai/
│           ├── AIOutbreakDashboard.java
│           ├── styles.css
│           └── components/
│               ├── PredictionCard.java
│               ├── SummaryCard.java
│               └── MetricComponent.java
│
├── docker-compose.ai.yml
├── start-ai-services.sh
├── README_AI_IMPLEMENTATION.md
├── DEPLOYMENT_GUIDE.md
├── PRD_01_EXECUTIVE_SUMMARY.md
├── PRD_02_BASE_SYSTEM_ARCHITECTURE.md
├── PRD_03_AI_MODULES_TECHNICAL_SPECS.md
└── PRD_04_IMPLEMENTATION_COMPLETE_GUIDE.md
```

---

## 7. Step-by-Step Implementation Guide

### Phase 1: Setup Foundation (Day 1)

1. **Create module structure**
   - Create sormas-ai-api directory and pom.xml
   - Create sormas-ai-backend directory and pom.xml
   - Create sormas-ai-models directory and structure
   - Create sormas-flow directory and pom.xml

2. **Update parent POM**
   - Edit sormas-base/pom.xml
   - Add modules section

3. **Update EAR POM**
   - Edit sormas-ear/pom.xml
   - Add AI backend dependency

### Phase 2: Implement Java API (Day 2)

1. **Create DTOs** (sormas-ai-api)
   - OutbreakPredictionDto.java
   - AIAnalysisRequestDto.java
   - Enum classes

2. **Create Facade Interface**
   - AIOutbreakPredictionFacade.java
   - Add JavaDoc comments

3. **Build and verify**
   - `mvn clean install -DskipTests`

### Phase 3: Implement Java Backend (Day 3-4)

1. **Create EJB Implementation**
   - AIOutbreakPredictionFacadeEjb.java
   - AIModelService.java (HTTP client)
   - CaseDataAnalysisService.java

2. **Add POC Algorithms**
   - Risk calculation
   - Case prediction
   - Recommendations

3. **Build and verify**
   - `mvn clean package`

### Phase 4: Implement Python Service (Day 5-6)

1. **Setup FastAPI Application**
   - app.py with routes
   - config.py for settings
   - requirements.txt

2. **Implement ML Models**
   - outbreak_predictor.py
   - risk_assessor.py
   - POC algorithms

3. **Create API Routes**
   - /predict/outbreak
   - /assess/risk
   - /health

4. **Test locally**
   - `uvicorn app:app --reload`
   - Test with curl

### Phase 5: Implement UI (Day 7-8)

1. **Create Vaadin Flow Dashboard**
   - AIOutbreakDashboard.java
   - Component methods
   - CSS styles

2. **Add CDI Injection**
   - Inject AIOutbreakPredictionFacade
   - Implement data loading

3. **Build and deploy**
   - `npm install`
   - `mvn clean package`

### Phase 6: Docker Setup (Day 9)

1. **Create Dockerfile**
   - For Python service

2. **Create docker-compose.ai.yml**
   - PostgreSQL
   - Python AI service
   - MLflow
   - Redis

3. **Create startup script**
   - start-ai-services.sh
   - Add health checks

4. **Test deployment**
   - `./start-ai-services.sh`
   - Verify all services

### Phase 7: Integration and Testing (Day 10-12)

1. **Integration testing**
   - Test Java ↔ Python communication
   - Test Dashboard ↔ Backend

2. **Performance testing**
   - Load test with Locust
   - Optimize queries

3. **Bug fixes**
   - Fix issues found in testing

### Phase 8: Documentation and Deployment (Day 13-14)

1. **Complete documentation**
   - README files
   - Deployment guides
   - API documentation

2. **Final deployment**
   - Deploy to server
   - Configure production settings
   - Setup monitoring

3. **Handover**
   - Demo to stakeholders
   - Training materials

---

## 8. Verification Checklist

### Module Build Verification

- [ ] sormas-ai-api builds successfully
- [ ] sormas-ai-backend builds successfully
- [ ] sormas-ai-models dependencies install
- [ ] sormas-flow builds successfully
- [ ] sormas-ear includes AI backend
- [ ] Full Maven build completes: `mvn clean install`

### Service Verification

- [ ] PostgreSQL starts and accepts connections
- [ ] Python AI service starts on port 8000
- [ ] MLflow starts on port 5000
- [ ] Redis starts on port 6379
- [ ] Health check returns 200: `curl http://localhost:8000/health`

### API Verification

- [ ] POST /predict/outbreak returns valid predictions
- [ ] POST /assess/risk returns risk assessments
- [ ] GET /models/versions returns model list
- [ ] GET /health returns healthy status
- [ ] Response times < 2 seconds

### Backend Verification

- [ ] AIOutbreakPredictionFacadeEjb deploys to Payara
- [ ] EJB injection works (no errors in logs)
- [ ] Predictions call Python service successfully
- [ ] Fallback to POC algorithm works when ML service down
- [ ] No errors in Payara logs

### UI Verification

- [ ] Dashboard loads at /sormas-flow/ai/outbreak-dashboard
- [ ] Disease filter dropdown populated
- [ ] Summary cards display correct counts
- [ ] Prediction cards display region data
- [ ] Refresh button works
- [ ] Retrain button triggers training
- [ ] No JavaScript console errors

### Integration Verification

- [ ] Java backend can call Python API
- [ ] Dashboard can call Java backend
- [ ] Predictions reflect current case data
- [ ] Risk levels calculated correctly
- [ ] Recommendations displayed properly

### Performance Verification

- [ ] Single prediction completes in < 2s
- [ ] Dashboard loads in < 3s
- [ ] 10 concurrent predictions complete successfully
- [ ] Memory usage stable over 1 hour
- [ ] No memory leaks detected

### Security Verification

- [ ] No secrets in code or git
- [ ] Environment variables used for config
- [ ] Database connections secure
- [ ] API endpoints accessible only from backend
- [ ] User permissions checked

### Documentation Verification

- [ ] README_AI_IMPLEMENTATION.md complete
- [ ] DEPLOYMENT_GUIDE.md accurate
- [ ] API endpoints documented
- [ ] Code has JavaDoc/docstrings
- [ ] PRD documents reviewed

---

## Conclusion

This comprehensive PRD provides all specifications needed for Lovable.dev to implement the AI-enhanced SORMAS outbreak prediction system. The implementation follows established patterns from the existing SORMAS codebase while introducing modern AI/ML capabilities.

**Key Implementation Principles:**
1. **Additive, not disruptive** - No changes to existing SORMAS modules
2. **Modular architecture** - Clean separation of concerns
3. **Graceful degradation** - POC fallback when ML service unavailable
4. **Production-ready** - Docker deployment, monitoring, logging
5. **Extensible** - Easy to add more ML models and features

**Next Steps:**
1. Review all PRD documents
2. Set up development environment
3. Follow step-by-step implementation guide
4. Use verification checklist for quality assurance
5. Deploy to server and gather feedback

---

**End of PRD Part 4: Complete Implementation Guide**

**All PRD Documents:**
1. PRD_01_EXECUTIVE_SUMMARY.md
2. PRD_02_BASE_SYSTEM_ARCHITECTURE.md
3. PRD_03_AI_MODULES_TECHNICAL_SPECS.md
4. PRD_04_IMPLEMENTATION_COMPLETE_GUIDE.md
