# Project Requirements Document (PRD)
# AI-Enhanced SORMAS: Outbreak Prediction System
## Part 1: Executive Summary & Overview

---

## Document Information

| Field | Value |
|-------|-------|
| **Project Name** | AI-Enhanced SORMAS Outbreak Prediction System |
| **Version** | 1.0.0-POC |
| **Date** | October 17, 2025 |
| **Repository** | https://github.com/danieltomabba/SORMAS-2.0 |
| **Branch** | feature/ai-outbreak-prediction |
| **License** | GPL-3.0 |
| **Document Status** | For Implementation by Lovable.dev |

---

## Table of Contents

### PRD Document Series
1. **PRD_01_EXECUTIVE_SUMMARY.md** (This Document)
2. PRD_02_BASE_SYSTEM_ARCHITECTURE.md
3. PRD_03_AI_MODULES_TECHNICAL_SPECS.md
4. PRD_04_API_CONTRACTS_INTEGRATION.md
5. PRD_05_UI_UX_REQUIREMENTS.md
6. PRD_06_INFRASTRUCTURE_DEPLOYMENT.md
7. PRD_07_DATA_MODELS_SCHEMAS.md
8. PRD_08_TESTING_QA_REQUIREMENTS.md

---

## 1. Executive Summary

### 1.1 Project Vision

Transform SORMAS (Surveillance Outbreak Response Management and Analysis System) from a traditional case management system into an AI-powered predictive surveillance platform capable of:

- **Predicting disease outbreaks** before they occur using machine learning
- **Assessing outbreak risk levels** in real-time across regions
- **Recommending intervention strategies** based on AI analysis
- **Tracking model performance** with MLOps best practices
- **Providing actionable insights** through modern, intuitive dashboards

### 1.2 Business Problem

Current SORMAS implementation:
- ✗ Reactive approach - responds after outbreaks are already established
- ✗ Limited predictive capabilities - relies on manual trend analysis
- ✗ No risk scoring - difficult to prioritize resource allocation
- ✗ Legacy UI (Vaadin 8) - outdated user experience
- ✗ No ML infrastructure - cannot leverage modern AI/ML techniques

### 1.3 Proposed Solution

Add AI-powered outbreak prediction capabilities to SORMAS through:

1. **Java EJB Backend** (sormas-ai-backend)
   - Integrates with existing SORMAS backend
   - Provides EJB facades for AI services
   - POC algorithms for risk assessment

2. **Python ML Microservice** (sormas-ai-models)
   - FastAPI-based REST service
   - Machine learning models for outbreak prediction
   - MLflow integration for model management

3. **Modern Web UI** (sormas-flow)
   - Vaadin Flow 24 (latest modern framework)
   - Interactive AI dashboard
   - Real-time prediction visualization

4. **API Contracts** (sormas-ai-api)
   - DTOs for data exchange
   - EJB facade interfaces
   - Type-safe contracts

### 1.4 Key Features

#### Outbreak Prediction
- Predict case counts 7-30 days ahead
- Confidence scoring for predictions
- Contributing factors analysis
- Disease-specific models

#### Risk Assessment
- Four-tier risk classification (LOW, MODERATE, HIGH, CRITICAL)
- Population-adjusted risk scoring
- Trend acceleration detection
- Alert level recommendations (NONE, WATCH, WARNING, EMERGENCY)

#### Modern Dashboard
- Real-time prediction updates
- Risk summary cards
- Disease filtering
- Interactive visualization
- Model retraining controls

#### MLOps Infrastructure
- Model versioning with MLflow
- Experiment tracking
- Performance monitoring
- A/B testing capability

---

## 2. Project Scope

### 2.1 In Scope

#### Phase 1: Core AI Infrastructure (Current - POC)
- ✓ AI backend module with EJB integration
- ✓ Python ML service with FastAPI
- ✓ Basic outbreak prediction algorithms
- ✓ Risk assessment models
- ✓ Modern Vaadin Flow UI
- ✓ Docker-based deployment
- ✓ MLflow integration
- ✓ Comprehensive documentation

#### Phase 2: Enhanced ML Models (Future)
- Advanced time-series forecasting (LSTM, Prophet)
- Multi-disease prediction models
- Geospatial risk mapping
- External data integration (weather, mobility)

#### Phase 3: Production Features (Future)
- Real-time streaming predictions
- Automated alerting system
- Natural language report generation
- Mobile app integration

### 2.2 Out of Scope

- Migration of existing SORMAS UI to Flow (coexistence model)
- Android app modifications
- Changes to core SORMAS case management
- Authentication/authorization changes
- Existing REST API modifications

### 2.3 Success Criteria

#### Technical Success
- ✓ All modules build without errors
- ✓ Docker services start successfully
- ✓ AI API responds to requests
- ✓ Dashboard loads and displays predictions
- ✓ Integration with existing SORMAS backend

#### Functional Success
- Predictions generated within 2 seconds
- Risk scores accurate within ±15% (POC)
- Dashboard loads in < 3 seconds
- Model retraining completes in < 5 minutes

#### User Success
- Public health officials can view predictions
- Risk levels are clearly communicated
- Recommendations are actionable
- Dashboard is intuitive without training

---

## 3. Stakeholders

### 3.1 Primary Stakeholders

| Role | Responsibilities | Success Criteria |
|------|------------------|------------------|
| **Public Health Officials** | Use predictions for decision-making | Actionable insights, clear risk levels |
| **Epidemiologists** | Validate model accuracy | Scientifically sound predictions |
| **System Administrators** | Deploy and maintain system | Easy deployment, stable operation |
| **Data Scientists** | Train and improve models | MLflow integration, experiment tracking |

### 3.2 Technical Stakeholders

| Role | Responsibilities |
|------|------------------|
| **Backend Developers** | Maintain Java EJB services |
| **Data Engineers** | Manage ML pipelines |
| **Frontend Developers** | Enhance Vaadin Flow UI |
| **DevOps Engineers** | Infrastructure and deployment |

---

## 4. Technical Overview

### 4.1 Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌────────────────┐              ┌────────────────┐         │
│  │  Vaadin Flow   │              │  Legacy UI     │         │
│  │  Dashboard     │              │  (Vaadin 8)    │         │
│  │  (NEW)         │              │  (Existing)    │         │
│  └────────────────┘              └────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│  ┌────────────────┐              ┌────────────────┐         │
│  │  AI Backend    │              │  SORMAS        │         │
│  │  EJB Facade    │◄────────────►│  Backend       │         │
│  │  (NEW)         │              │  (Existing)    │         │
│  └────────────────┘              └────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────────┐
│                     ML Services Layer                        │
│  ┌────────────────┐              ┌────────────────┐         │
│  │  Python ML     │              │  MLflow        │         │
│  │  Service       │◄────────────►│  Tracking      │         │
│  │  (FastAPI)     │              │  Server        │         │
│  └────────────────┘              └────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                               │
│  ┌────────────────┐              ┌────────────────┐         │
│  │  PostgreSQL    │              │  Redis         │         │
│  │  Database      │              │  Cache         │         │
│  └────────────────┘              └────────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Backend** | Java EE | 8 | Enterprise Java Beans |
| **JDK** | OpenJDK | 11 | Java runtime |
| **Build Tool** | Maven | 3.6.3+ | Build automation |
| **Application Server** | Payara | 5.2022.5 | Java EE container |
| **Legacy UI** | Vaadin | 8.14.3 | Existing UI framework |
| **Modern UI** | Vaadin Flow | 24.3.0 | New AI dashboard |
| **ML Service** | Python | 3.11 | ML runtime |
| **API Framework** | FastAPI | 0.104+ | Python REST API |
| **ML Framework** | scikit-learn | 1.3+ | Machine learning |
| **Deep Learning** | TensorFlow | 2.14+ | Neural networks |
| **Deep Learning** | PyTorch | 2.1+ | Neural networks |
| **Time Series** | Prophet | 1.1+ | Forecasting |
| **ML Tracking** | MLflow | 2.8+ | Model management |
| **Database** | PostgreSQL | 15 | Primary database |
| **Cache** | Redis | 7.2 | Caching layer |
| **Containerization** | Docker | 20.10+ | Deployment |
| **Orchestration** | Docker Compose | 2.0+ | Multi-container apps |

### 4.3 Module Overview

#### New Modules (AI Enhancement)

1. **sormas-ai-api** (Java)
   - Location: `/sormas-ai-api`
   - Type: JAR library
   - Purpose: API contracts and DTOs
   - Dependencies: sormas-api, Jackson

2. **sormas-ai-backend** (Java)
   - Location: `/sormas-ai-backend`
   - Type: EJB JAR
   - Purpose: AI business logic
   - Dependencies: sormas-api, sormas-ai-api, sormas-backend

3. **sormas-ai-models** (Python)
   - Location: `/sormas-ai-models`
   - Type: Python service
   - Purpose: ML models and predictions
   - Dependencies: FastAPI, scikit-learn, TensorFlow, PyTorch, MLflow

4. **sormas-flow** (Java)
   - Location: `/sormas-flow`
   - Type: WAR application
   - Purpose: Modern web UI
   - Dependencies: Vaadin Flow 24, sormas-ai-api

#### Existing Modules (No Changes)

- sormas-api: Core API definitions
- sormas-backend: Backend services
- sormas-ui: Legacy Vaadin 8 UI
- sormas-rest: REST API
- sormas-app: Android application
- sormas-ear: Enterprise archive
- sormas-base: Base configuration

---

## 5. High-Level Requirements

### 5.1 Functional Requirements

#### FR-1: Outbreak Prediction
- **FR-1.1**: System shall predict outbreak cases for 7, 14, and 30-day horizons
- **FR-1.2**: System shall provide confidence scores (0.0 to 1.0) for predictions
- **FR-1.3**: System shall identify top contributing factors
- **FR-1.4**: System shall support all SORMAS diseases (COVID-19, Ebola, Dengue, etc.)

#### FR-2: Risk Assessment
- **FR-2.1**: System shall classify regions into 4 risk levels (LOW, MODERATE, HIGH, CRITICAL)
- **FR-2.2**: System shall calculate risk scores (0.0 to 1.0)
- **FR-2.3**: System shall recommend alert levels (NONE, WATCH, WARNING, EMERGENCY)
- **FR-2.4**: System shall provide actionable recommendations per risk level

#### FR-3: Dashboard Visualization
- **FR-3.1**: Dashboard shall display summary cards with risk counts
- **FR-3.2**: Dashboard shall show prediction cards by region
- **FR-3.3**: Dashboard shall support disease filtering
- **FR-3.4**: Dashboard shall provide manual refresh capability
- **FR-3.5**: Dashboard shall display confidence and contributing factors

#### FR-4: Model Management
- **FR-4.1**: System shall support model versioning
- **FR-4.2**: System shall track model performance metrics
- **FR-4.3**: System shall support model retraining triggers
- **FR-4.4**: System shall log all experiments in MLflow

### 5.2 Non-Functional Requirements

#### NFR-1: Performance
- **NFR-1.1**: Prediction requests shall complete within 2 seconds (95th percentile)
- **NFR-1.2**: Dashboard shall load within 3 seconds
- **NFR-1.3**: System shall support 100 concurrent users
- **NFR-1.4**: Database queries shall complete within 500ms

#### NFR-2: Scalability
- **NFR-2.1**: System shall handle 1000+ regions
- **NFR-2.2**: ML service shall be horizontally scalable
- **NFR-2.3**: Database shall support 10M+ case records

#### NFR-3: Reliability
- **NFR-3.1**: System uptime shall be ≥99.5%
- **NFR-3.2**: Failed predictions shall not crash the system
- **NFR-3.3**: System shall gracefully degrade if ML service is unavailable

#### NFR-4: Maintainability
- **NFR-4.1**: Code shall follow SORMAS coding standards
- **NFR-4.2**: All APIs shall be documented
- **NFR-4.3**: Modules shall be loosely coupled
- **NFR-4.4**: Docker deployment shall be automated

#### NFR-5: Security
- **NFR-5.1**: All API endpoints shall require authentication (future)
- **NFR-5.2**: Predictions shall respect user access controls
- **NFR-5.3**: Environment variables shall not contain secrets in code
- **NFR-5.4**: Database credentials shall be encrypted

---

## 6. Integration Points

### 6.1 SORMAS Backend Integration

```java
// AI Backend depends on existing SORMAS services
@EJB
private CaseFacadeEjb caseFacade;

@EJB
private RegionFacadeEjb regionFacade;

@EJB
private PopulationDataFacadeEjb populationDataFacade;
```

**Integration Requirements:**
- AI backend must use existing EJB facades
- No modifications to SORMAS core services
- Read-only access to case data
- Respect existing user permissions

### 6.2 Database Integration

```sql
-- AI Backend reads from existing tables
SELECT * FROM cases WHERE disease = 'CORONAVIRUS';
SELECT * FROM regions WHERE uuid IN (...);
SELECT * FROM populationdata WHERE region_id = ?;

-- No new tables in SORMAS schema (POC)
-- Future: Dedicated AI schema for predictions
```

### 6.3 UI Integration

**Coexistence Model:**
- Legacy Vaadin 8 UI remains unchanged
- New Vaadin Flow UI runs alongside
- Separate context paths:
  - Legacy: `/sormas-ui`
  - AI Dashboard: `/sormas-flow/ai/outbreak-dashboard`

---

## 7. Data Flow

### 7.1 Prediction Request Flow

```
┌──────────┐         ┌──────────┐         ┌──────────┐         ┌──────────┐
│  User    │────────►│  Flow UI │────────►│  AI EJB  │────────►│  SORMAS  │
│          │ Request │          │  Inject │  Facade  │  Query  │  Backend │
└──────────┘         └──────────┘         └──────────┘         └──────────┘
                            │                   │                      │
                            │                   ▼                      ▼
                            │         ┌──────────────────┐    ┌──────────────┐
                            │         │  Python ML       │    │  PostgreSQL  │
                            │◄────────│  Service         │    │  Database    │
                            │ Display │  (FastAPI)       │    └──────────────┘
                            │         └──────────────────┘
                            │                   │
                            │                   ▼
                            │         ┌──────────────────┐
                            │         │  MLflow          │
                            │         │  Tracking        │
                            │         └──────────────────┘
```

### 7.2 Model Training Flow

```
┌──────────┐         ┌──────────┐         ┌──────────┐
│  Admin   │────────►│  Dashboard│────────►│  AI EJB  │
│          │ Trigger │          │  Call   │          │
└──────────┘         └──────────┘         └──────────┘
                                                 │
                                                 ▼
                                     ┌──────────────────┐
                                     │  Python ML       │
                                     │  Service         │
                                     │  /models/retrain │
                                     └──────────────────┘
                                                 │
                     ┌───────────────────────────┼───────────────────┐
                     ▼                           ▼                   ▼
           ┌──────────────┐          ┌──────────────┐     ┌──────────────┐
           │  Load Data   │          │  Train Model │     │  Register in │
           │  from DB     │─────────►│  Validate    │────►│  MLflow      │
           └──────────────┘          └──────────────┘     └──────────────┘
```

---

## 8. Deployment Architecture

### 8.1 Container Architecture

```
Docker Compose Network: sormas-ai-network

┌─────────────────────────────────────────────────────────────┐
│  Host Machine                                                │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  PostgreSQL  │  │  Redis       │  │  MLflow      │      │
│  │  :5432       │  │  :6379       │  │  :5000       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
│                  ┌──────────────────┐                        │
│                  │  Python ML       │                        │
│                  │  Service         │                        │
│                  │  :8000           │                        │
│                  └──────────────────┘                        │
│                            │                                 │
│                  ┌──────────────────┐                        │
│                  │  Payara Server   │                        │
│                  │  :6080, :4848    │                        │
│                  │  - sormas-ear    │                        │
│                  │  - sormas-flow   │                        │
│                  └──────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 Port Mapping

| Service | Container Port | Host Port | Purpose |
|---------|---------------|-----------|---------|
| PostgreSQL | 5432 | 5432 | Database |
| Redis | 6379 | 6379 | Cache |
| AI Service | 8000 | 8000 | ML predictions |
| MLflow | 5000 | 5000 | Model tracking |
| Payara | 8080 | 6080 | SORMAS application |
| Payara Admin | 4848 | 4848 | Admin console |

---

## 9. Implementation Roadmap

### Phase 1: Foundation (Completed)
- ✓ Create AI module structure
- ✓ Implement POC prediction algorithms
- ✓ Build Vaadin Flow dashboard
- ✓ Setup Docker infrastructure
- ✓ Integrate MLflow
- ✓ Write documentation

### Phase 2: Enhancement (Future)
- Advanced ML models (LSTM, Prophet)
- Real-time predictions
- Geospatial visualization
- External data integration
- Performance optimization

### Phase 3: Production (Future)
- Authentication/authorization
- Monitoring and alerting
- Automated testing
- CI/CD pipeline
- Mobile integration

---

## 10. Risk and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Model accuracy low | High | Medium | Use POC label, validate with experts |
| Performance issues | Medium | Low | Implement caching, optimize queries |
| Integration conflicts | High | Low | Use separate modules, minimal coupling |
| Deployment complexity | Medium | Medium | Docker automation, detailed guides |
| Data quality issues | High | Medium | Data validation, error handling |

---

## 11. Documentation Deliverables

### 11.1 Technical Documentation
- ✓ README_AI_IMPLEMENTATION.md
- ✓ DEPLOYMENT_GUIDE.md
- ✓ This PRD series (8 documents)
- ✓ Inline code documentation
- ✓ API documentation (FastAPI auto-generated)

### 11.2 User Documentation
- Dashboard user guide (future)
- Model interpretation guide (future)
- Best practices for public health officials (future)

---

## 12. Next Steps

1. **Review this PRD** with stakeholders
2. **Proceed to PRD_02** for detailed base system architecture
3. **Implement using Lovable.dev** following specifications
4. **Test deployment** on target server
5. **Gather feedback** from users
6. **Iterate** based on results

---

## Document Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-10-17 | Claude Code | Initial comprehensive PRD |

---

**End of Part 1: Executive Summary & Overview**

**Next Document**: PRD_02_BASE_SYSTEM_ARCHITECTURE.md
