# SORMAS AI Implementation - Project Requirements Documents (PRD) Index

## Overview

This is a comprehensive 4-part Project Requirements Document series for implementing AI-powered outbreak prediction capabilities in SORMAS 2.0. These documents are specifically designed for external implementation teams (like Lovable.dev) who don't have direct GitHub access.

---

## Document Series

### Part 1: Executive Summary & Overview
**File**: [PRD_01_EXECUTIVE_SUMMARY.md](./PRD_01_EXECUTIVE_SUMMARY.md)
**Size**: ~51 KB | **Pages**: ~45

**Contents:**
- Project vision and business objectives
- Problem statement and proposed solution
- Key features and capabilities
- High-level architecture overview
- Technology stack summary
- Stakeholder analysis
- Success criteria and metrics
- Risk assessment and mitigation
- Implementation roadmap
- Next steps and deliverables

**Use This When:**
- First understanding the project
- Presenting to stakeholders
- Getting executive buy-in
- Planning resources and timeline

---

### Part 2: Base System Architecture
**File**: [PRD_02_BASE_SYSTEM_ARCHITECTURE.md](./PRD_02_BASE_SYSTEM_ARCHITECTURE.md)
**Size**: ~58 KB | **Pages**: ~50

**Contents:**
- SORMAS base system overview
- Existing module architecture (sormas-api, sormas-backend, sormas-ui, etc.)
- Technology stack deep dive
  - Java EE 8, EJB 3.2, JPA 2.2
  - Vaadin 8.14.3 (legacy) and Vaadin Flow 24.3.0 (modern)
  - PostgreSQL 15 with PostGIS
  - Payara Server 5.2022.5
- Database architecture and schemas
- Maven build system
- Deployment models (traditional and Docker)
- Integration patterns (EJB injection, CDI, HTTP clients)
- Security and authorization model

**Use This When:**
- Understanding existing SORMAS architecture
- Planning integration points
- Setting up development environment
- Making architectural decisions

---

### Part 3: AI Modules Technical Specifications
**File**: [PRD_03_AI_MODULES_TECHNICAL_SPECS.md](./PRD_03_AI_MODULES_TECHNICAL_SPECS.md)
**Size**: ~70 KB | **Pages**: ~60

**Contents:**
- Complete specifications for 4 new modules:

1. **sormas-ai-api** (Java JAR)
   - DTOs: OutbreakPredictionDto, AIAnalysisRequestDto
   - Enums: RiskLevel, AlertLevel, AnalysisType
   - Facade interface: AIOutbreakPredictionFacade
   - Complete code examples

2. **sormas-ai-backend** (Java EJB)
   - EJB implementation: AIOutbreakPredictionFacadeEjb
   - Service classes: AIModelService, CaseDataAnalysisService
   - POC algorithms for risk assessment
   - HTTP client for Python ML service
   - Complete code with fallback logic

3. **sormas-ai-models** (Python FastAPI)
   - FastAPI application structure
   - ML models: OutbreakPredictor, RiskAssessor
   - API routes and schemas
   - MLflow integration
   - Complete Python code examples

4. **sormas-flow** (Vaadin Flow WAR)
   - Modern UI dashboard
   - Component specifications
   - CSS styling
   - CDI injection patterns

**Use This When:**
- Implementing the AI modules
- Writing code
- Understanding ML algorithms
- Debugging issues

---

### Part 4: Complete Implementation Guide
**File**: [PRD_04_IMPLEMENTATION_COMPLETE_GUIDE.md](./PRD_04_IMPLEMENTATION_COMPLETE_GUIDE.md)
**Size**: ~82 KB | **Pages**: ~70

**Contents:**
- **API Contracts and Integration**
  - FastAPI endpoint specifications with JSON schemas
  - cURL examples
  - Java ↔ Python communication patterns
  - Integration testing scenarios

- **UI/UX Requirements**
  - Complete dashboard layout
  - Vaadin Flow component code
  - CSS styling specifications
  - User interaction flows

- **Infrastructure and Deployment**
  - Dockerfile for Python service
  - docker-compose.ai.yml with all services
  - startup scripts with health checks
  - Step-by-step deployment procedures

- **Data Models and Schemas**
  - Database schema SQL (future enhancement)
  - JPA entity classes
  - JSON data structures

- **Testing Requirements**
  - Unit test examples (Java and Python)
  - Integration test scenarios
  - Performance test specifications
  - Test coverage requirements

- **Complete File Structure**
  - Every file and directory
  - Package organization

- **Step-by-Step Implementation Guide**
  - 14-day implementation plan
  - Phase-by-phase breakdown
  - Daily tasks and deliverables

- **Verification Checklist**
  - 50+ verification points
  - Module build checks
  - Service health checks
  - API endpoint verification
  - Performance validation

**Use This When:**
- Starting implementation
- Building Docker infrastructure
- Writing tests
- Deploying to servers
- Verifying completeness

---

## Quick Start Guide

### For Project Managers
1. Read **Part 1** for overview and planning
2. Review **Part 4** Step-by-Step Guide for timeline
3. Use **Part 4** Verification Checklist for milestones

### For Architects
1. Read **Part 1** for system overview
2. Study **Part 2** for integration points
3. Review **Part 3** for module dependencies

### For Developers
1. Skim **Part 1** for context
2. Reference **Part 2** for existing patterns
3. Implement using **Part 3** and **Part 4**
4. Use **Part 4** Verification Checklist

### For QA Engineers
1. Read **Part 1** for requirements
2. Extract test scenarios from **Part 3**
3. Follow **Part 4** Testing Requirements
4. Use **Part 4** Verification Checklist

---

## Key Files and Their Locations

### Repository Information
- **Repository**: https://github.com/danieltomabba/SORMAS-2.0
- **Branch**: `feature/ai-outbreak-prediction`
- **Base Directory**: `/Users/dtomabba/Desktop/SORMAS-2.0`

### Module Locations
```
SORMAS-2.0/
├── sormas-ai-api/              # Java DTOs and interfaces
├── sormas-ai-backend/          # Java EJB implementation
├── sormas-ai-models/           # Python ML service
├── sormas-flow/                # Vaadin Flow UI
├── sormas-base/               # Parent POM (updated)
├── sormas-ear/                # EAR packaging (updated)
└── docker-compose.ai.yml      # Docker services
```

### Key Configuration Files
- `sormas-base/pom.xml` - Maven parent (includes AI modules)
- `sormas-ear/pom.xml` - EAR packaging (includes AI backend)
- `sormas-ai-models/.env` - Python service configuration
- `docker-compose.ai.yml` - Docker orchestration
- `start-ai-services.sh` - Automated startup script

### Documentation Files
- `README_AI_IMPLEMENTATION.md` - Implementation guide
- `DEPLOYMENT_GUIDE.md` - Server deployment
- `PRD_01_EXECUTIVE_SUMMARY.md` - Project overview
- `PRD_02_BASE_SYSTEM_ARCHITECTURE.md` - Base system
- `PRD_03_AI_MODULES_TECHNICAL_SPECS.md` - AI modules
- `PRD_04_IMPLEMENTATION_COMPLETE_GUIDE.md` - Complete guide
- `PRD_INDEX.md` - This file

---

## Technology Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| **Backend** | Java EE | 8 |
| **JDK** | OpenJDK | 11 |
| **Build** | Maven | 3.6.3+ |
| **App Server** | Payara | 5.2022.5 |
| **Legacy UI** | Vaadin | 8.14.3 |
| **Modern UI** | Vaadin Flow | 24.3.0 |
| **ML Service** | Python | 3.11 |
| **API Framework** | FastAPI | 0.104+ |
| **ML Library** | scikit-learn | 1.3+ |
| **ML Tracking** | MLflow | 2.8+ |
| **Database** | PostgreSQL | 15 |
| **Cache** | Redis | 7.2 |
| **Container** | Docker | 20.10+ |

---

## Implementation Timeline

### Phase 1: Setup (Day 1)
- Create module structure
- Update parent POMs
- Setup Docker environment

### Phase 2: Java API (Day 2)
- Implement DTOs
- Create facade interface
- Build and verify

### Phase 3: Java Backend (Day 3-4)
- Implement EJB facade
- Add POC algorithms
- Test integration

### Phase 4: Python Service (Day 5-6)
- Setup FastAPI
- Implement ML models
- Create API routes

### Phase 5: UI (Day 7-8)
- Build Vaadin Flow dashboard
- Add components
- Style with CSS

### Phase 6: Docker (Day 9)
- Create Dockerfile
- Setup docker-compose
- Test deployment

### Phase 7: Testing (Day 10-12)
- Integration tests
- Performance tests
- Bug fixes

### Phase 8: Deployment (Day 13-14)
- Documentation
- Server deployment
- Training and handover

---

## Success Metrics

### Technical Metrics
- All modules build without errors
- API response time < 2 seconds (P95)
- Dashboard load time < 3 seconds
- Test coverage > 80%
- Zero critical bugs

### Functional Metrics
- Predictions generated successfully
- Risk levels calculated accurately
- Dashboard displays all data
- Recommendations are actionable

### User Metrics
- Public health officials can view predictions
- No training required for basic use
- Clear visual indicators for risk levels

---

## Support and Resources

### Documentation
- Full implementation guide: `README_AI_IMPLEMENTATION.md`
- Deployment guide: `DEPLOYMENT_GUIDE.md`
- API docs: http://localhost:8000/docs (when running)

### Testing
- Health check: `curl http://localhost:8000/health`
- Prediction test: See PRD_04 API examples
- Load testing: Locust scripts in PRD_04

### Troubleshooting
- Check Docker logs: `docker-compose logs -f sormas-ai`
- Verify services: `docker-compose ps`
- Check Payara logs: `$PAYARA_HOME/glassfish/domains/domain1/logs/server.log`

---

## Document Statistics

| Document | Size | Pages | Lines | Focus Area |
|----------|------|-------|-------|------------|
| Part 1 | 51 KB | 45 | 620 | Overview & Planning |
| Part 2 | 58 KB | 50 | 800 | Base Architecture |
| Part 3 | 70 KB | 60 | 1,200 | AI Module Specs |
| Part 4 | 82 KB | 70 | 1,400 | Implementation |
| **Total** | **261 KB** | **225** | **4,020** | **Complete System** |

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-10-17 | Claude Code | Initial comprehensive PRD series |

---

## License

This project follows the same GPL-3.0 license as SORMAS.

---

## Contact

For questions or clarifications about these requirements documents:
- GitHub Issues: https://github.com/danieltomabba/SORMAS-2.0/issues
- Branch: `feature/ai-outbreak-prediction`

---

**🤖 Generated with [Claude Code](https://claude.com/claude-code)**

**Note**: These documents are designed to be self-contained and provide all information needed for implementation without GitHub access. All code examples, configurations, and specifications are included in the documents themselves.
