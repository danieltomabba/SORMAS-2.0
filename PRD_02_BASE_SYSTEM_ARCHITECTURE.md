# Project Requirements Document (PRD)
# AI-Enhanced SORMAS: Outbreak Prediction System
## Part 2: Base System Architecture (SORMAS 2.0)

---

## Document Information

| Field | Value |
|-------|-------|
| **Document** | PRD Part 2 of 8 |
| **Version** | 1.0.0 |
| **Date** | October 17, 2025 |
| **Dependencies** | PRD_01_EXECUTIVE_SUMMARY.md |

---

## Table of Contents

1. [SORMAS Base System Overview](#1-sormas-base-system-overview)
2. [Existing Module Architecture](#2-existing-module-architecture)
3. [Technology Stack Details](#3-technology-stack-details)
4. [Database Architecture](#4-database-architecture)
5. [Build System](#5-build-system)
6. [Deployment Model](#6-deployment-model)
7. [Integration Patterns](#7-integration-patterns)
8. [Security Model](#8-security-model)

---

## 1. SORMAS Base System Overview

### 1.1 System Purpose

SORMAS is an open-source eHealth system designed for:
- Disease surveillance and outbreak response
- Contact tracing and follow-up
- Case investigation and management
- Laboratory sample tracking
- Event-based surveillance
- Immunization tracking
- Campaign management

### 1.2 System Components

```
SORMAS Ecosystem
├── Web Application (sormas-ui)
├── REST API (sormas-rest)
├── Android Application (sormas-app)
├── Backend Services (sormas-backend)
└── Integration APIs (Sormas2Sormas)
```

### 1.3 Supported Diseases

SORMAS supports 40+ diseases including:
- COVID-19 (CORONAVIRUS)
- Ebola Virus Disease (EVD)
- Dengue Fever (DENGUE)
- Measles (MEASLES)
- Cholera (CHOLERA)
- Yellow Fever (YELLOW_FEVER)
- Lassa Fever (LASSA)
- Monkeypox (MONKEYPOX)
- And many more...

**Requirement for AI Module:**
The AI prediction system must support all diseases defined in the `Disease` enum.

---

## 2. Existing Module Architecture

### 2.1 Core Modules

#### 2.1.1 sormas-api

**Location**: `/sormas-api`
**Type**: JAR library
**Purpose**: API definitions and DTOs shared between all modules

**Key Components:**
```
sormas-api/src/main/java/de/symeda/sormas/api/
├── caze/                    # Case management
│   ├── CaseDataDto.java
│   ├── CaseFacade.java
│   └── CaseCriteria.java
├── contact/                 # Contact tracing
│   ├── ContactDto.java
│   └── ContactFacade.java
├── event/                   # Event surveillance
│   ├── EventDto.java
│   └── EventFacade.java
├── Disease.java             # Disease enum
├── region/                  # Geographic regions
│   ├── RegionDto.java
│   ├── RegionReferenceDto.java
│   └── RegionFacade.java
├── person/                  # Person data
│   ├── PersonDto.java
│   └── PersonFacade.java
└── utils/                   # Utilities
    ├── DataHelper.java
    └── DateHelper.java
```

**Key DTOs:**
```java
// Example: CaseDataDto
public class CaseDataDto extends EntityDto {
    private Disease disease;
    private String diseaseDetails;
    private PersonReferenceDto person;
    private RegionReferenceDto region;
    private DistrictReferenceDto district;
    private Date reportDate;
    private CaseClassification caseClassification;
    private InvestigationStatus investigationStatus;
    // ... 100+ fields
}
```

**AI Module Dependency:**
- `sormas-ai-api` depends on `sormas-api`
- Uses `RegionReferenceDto`, `Disease` enum
- Follows same DTO patterns

#### 2.1.2 sormas-backend

**Location**: `/sormas-backend`
**Type**: EJB JAR
**Purpose**: Business logic and data access

**Key Components:**
```
sormas-backend/src/main/java/de/symeda/sormas/backend/
├── caze/
│   ├── Case.java                    # JPA Entity
│   ├── CaseService.java             # Data access
│   ├── CaseFacadeEjb.java          # EJB facade
│   └── CaseJoins.java              # Query joins
├── contact/
│   ├── Contact.java
│   ├── ContactService.java
│   └── ContactFacadeEjb.java
├── region/
│   ├── Region.java
│   ├── RegionService.java
│   └── RegionFacadeEjb.java
├── infrastructure/
│   ├── PopulationData.java
│   └── PopulationDataService.java
└── common/
    ├── AbstractDomainObject.java   # Base entity
    └── AbstractAdoService.java     # Base service
```

**EJB Pattern:**
```java
@Stateless(name = "CaseFacade")
public class CaseFacadeEjb implements CaseFacade {

    @EJB
    private CaseService caseService;

    @PersistenceContext(unitName = "sormasPU")
    private EntityManager em;

    @Override
    public CaseDataDto getCaseDataByUuid(String uuid) {
        Case caze = caseService.getByUuid(uuid);
        return toDto(caze);
    }

    // ... hundreds of business methods
}
```

**AI Module Integration:**
```java
// sormas-ai-backend uses same pattern
@Stateless(name = "AIOutbreakPredictionFacade")
public class AIOutbreakPredictionFacadeEjb implements AIOutbreakPredictionFacade {

    @EJB
    private CaseFacadeEjb caseFacade;  // Inject existing facade

    @EJB
    private RegionFacadeEjb regionFacade;

    // AI-specific business logic
}
```

#### 2.1.3 sormas-ui

**Location**: `/sormas-ui`
**Type**: WAR application
**Purpose**: Legacy Vaadin 8 web interface

**Key Components:**
```
sormas-ui/src/main/java/de/symeda/sormas/ui/
├── dashboard/
│   ├── surveillance/
│   │   └── SurveillanceDashboardView.java
│   └── contacts/
│       └── ContactsDashboardView.java
├── caze/
│   ├── CaseDataView.java
│   ├── CaseController.java
│   └── CasesView.java
├── contact/
│   ├── ContactDataView.java
│   └── ContactsView.java
└── SormasUI.java                    # Main UI class
```

**Vaadin 8 Pattern:**
```java
@SpringView(name = CasesView.VIEW_NAME)
public class CasesView extends AbstractListView<CaseDataDto, CaseCriteria, CaseGrid> {

    public static final String VIEW_NAME = "cases";

    public CasesView() {
        super(CaseDataDto.class);
    }

    @Override
    protected Component createFilterBar() {
        // Create filter components
    }

    @Override
    protected CaseGrid createGrid() {
        // Create data grid
    }
}
```

**AI Module Approach:**
- **Do NOT modify sormas-ui**
- Create separate **sormas-flow** module
- Use modern Vaadin Flow 24
- Coexist with legacy UI

#### 2.1.4 sormas-rest

**Location**: `/sormas-rest`
**Type**: WAR application
**Purpose**: REST API for external systems

**Key Components:**
```
sormas-rest/src/main/java/de/symeda/sormas/rest/
├── CaseResource.java
├── ContactResource.java
├── EventResource.java
└── security/
    ├── KeycloakFilter.java
    └── SecurityConfig.java
```

**REST Pattern:**
```java
@Path("/cases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaseResource {

    @EJB
    private CaseFacade caseFacade;

    @GET
    @Path("/{uuid}")
    public Response getCaseByUuid(@PathParam("uuid") String uuid) {
        CaseDataDto caseData = caseFacade.getCaseDataByUuid(uuid);
        return Response.ok(caseData).build();
    }
}
```

**AI Module:**
- **Do NOT add endpoints to sormas-rest**
- Python ML service has its own FastAPI
- Java AI backend uses EJB injection only

#### 2.1.5 sormas-ear

**Location**: `/sormas-ear`
**Type**: EAR (Enterprise Archive)
**Purpose**: Package all modules for deployment

**Structure:**
```xml
<ear>
  <modules>
    <ejbModule>
      <groupId>de.symeda.sormas</groupId>
      <artifactId>sormas-backend</artifactId>
      <bundleFileName>sormas-backend.jar</bundleFileName>
    </ejbModule>
    <ejbModule>
      <groupId>de.symeda.sormas</groupId>
      <artifactId>sormas-ai-backend</artifactId>  <!-- NEW -->
      <bundleFileName>sormas-ai-backend.jar</bundleFileName>
    </ejbModule>
    <webModule>
      <groupId>de.symeda.sormas</groupId>
      <artifactId>sormas-rest</artifactId>
      <bundleFileName>sormas-rest.war</bundleFileName>
    </webModule>
    <webModule>
      <groupId>de.symeda.sormas</groupId>
      <artifactId>sormas-ui</artifactId>
      <bundleFileName>sormas-ui.war</bundleFileName>
    </webModule>
  </modules>
</ear>
```

**AI Impact:**
- Add `sormas-ai-backend` as EJB module
- Deploy `sormas-flow` as separate WAR (optional, can be standalone)

#### 2.1.6 sormas-app

**Location**: `/sormas-app`
**Type**: Android APK
**Purpose**: Mobile field data collection

**AI Module:**
- **No changes to sormas-app**
- Mobile AI features are future phase

#### 2.1.7 sormas-base

**Location**: `/sormas-base`
**Type**: Maven parent POM
**Purpose**: Multi-module project configuration

**Modified for AI:**
```xml
<modules>
    <!-- Existing modules -->
    <module>../sormas-api</module>
    <module>../sormas-backend</module>
    <module>../sormas-ui</module>
    <module>../sormas-rest</module>
    <module>../sormas-ear</module>
    <module>../sormas-app</module>

    <!-- AI Modules - NEW -->
    <module>../sormas-ai-api</module>
    <module>../sormas-ai-backend</module>
    <module>../sormas-flow</module>
</modules>
```

---

## 3. Technology Stack Details

### 3.1 Java Stack

#### JDK Version
```bash
$ java -version
openjdk version "11.0.x"
OpenJDK Runtime Environment
```

**Requirements:**
- Minimum: JDK 11
- Recommended: JDK 11 LTS
- Not compatible: JDK 17+ (dependency issues)

#### Jakarta EE / Java EE
```xml
<dependency>
    <groupId>javax</groupId>
    <artifactId>javaee-api</artifactId>
    <version>8.0</version>
    <scope>provided</scope>
</dependency>
```

**Specifications Used:**
- EJB 3.2 (Enterprise Java Beans)
- JPA 2.2 (Java Persistence API)
- CDI 2.0 (Contexts and Dependency Injection)
- JAX-RS 2.1 (REST endpoints)
- Servlet 4.0
- JSF 2.3 (limited use)

#### Vaadin Versions

**Legacy UI (sormas-ui):**
```xml
<vaadin.version>8.14.3</vaadin.version>
```
- Last maintained version of Vaadin 8
- Server-side rendering
- GWT-based widgetset compilation

**Modern UI (sormas-flow - NEW):**
```xml
<vaadin.version>24.3.0</vaadin.version>
```
- Latest Vaadin Flow version
- Web components
- No GWT, uses LitElement
- Better performance
- Modern look and feel

### 3.2 Database Stack

#### PostgreSQL Version
```
PostgreSQL 15.x (minimum 12.x)
```

**Extensions Used:**
```sql
CREATE EXTENSION IF NOT EXISTS postgis;      -- Geospatial
CREATE EXTENSION IF NOT EXISTS pg_trgm;      -- Text search
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- UUID generation
```

#### JDBC Driver
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.5.1</version>
</dependency>
```

#### Hibernate ORM
```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.4.32.Final</version>
</dependency>
```

**Persistence Configuration:**
```xml
<!-- persistence.xml -->
<persistence-unit name="sormasPU" transaction-type="JTA">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <jta-data-source>jdbc/sormasDS</jta-data-source>

    <properties>
        <property name="hibernate.dialect" value="de.symeda.sormas.backend.ExtendedPostgreSQL94Dialect"/>
        <property name="hibernate.show_sql" value="false"/>
        <property name="hibernate.format_sql" value="true"/>
    </properties>
</persistence-unit>
```

### 3.3 Application Server

#### Payara Server
```
Version: 5.2022.5 (based on GlassFish 5.x)
Java EE: 8.0
Profile: Full Profile
```

**Directory Structure:**
```
$PAYARA_HOME/
├── glassfish/
│   ├── domains/
│   │   └── domain1/
│   │       ├── autodeploy/          # Drop EAR/WAR here
│   │       ├── config/
│   │       │   └── domain.xml       # Server config
│   │       ├── logs/
│   │       │   └── server.log       # Application logs
│   │       └── applications/        # Deployed apps
│   └── lib/                         # Server libraries
└── bin/
    └── asadmin                      # Admin CLI
```

**JDBC Connection Pool:**
```xml
<!-- domain.xml -->
<jdbc-connection-pool
    name="sormasPool"
    datasource-classname="org.postgresql.ds.PGSimpleDataSource"
    res-type="javax.sql.DataSource">
    <property name="serverName" value="localhost"/>
    <property name="portNumber" value="5432"/>
    <property name="databaseName" value="sormas"/>
    <property name="user" value="sormas"/>
    <property name="password" value="sormas"/>
</jdbc-connection-pool>

<jdbc-resource
    jndi-name="jdbc/sormasDS"
    pool-name="sormasPool"/>
```

---

## 4. Database Architecture

### 4.1 Core Tables

#### Cases Table
```sql
CREATE TABLE cases (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    changedate TIMESTAMP NOT NULL,
    creationdate TIMESTAMP NOT NULL,

    -- Disease info
    disease VARCHAR(255) NOT NULL,
    diseasedetails TEXT,

    -- Person reference
    person_id BIGINT NOT NULL REFERENCES person(id),

    -- Location
    region_id BIGINT REFERENCES region(id),
    district_id BIGINT REFERENCES district(id),
    community_id BIGINT REFERENCES community(id),
    healthfacility_id BIGINT REFERENCES facility(id),

    -- Dates
    reportdate TIMESTAMP NOT NULL,
    investigateddate TIMESTAMP,

    -- Classification
    caseclassification VARCHAR(255),
    investigationstatus VARCHAR(255),
    outcome VARCHAR(255),

    -- ... 100+ more columns

    sys_period tstzrange  -- Temporal tracking
);

-- Indexes
CREATE INDEX idx_cases_disease ON cases(disease);
CREATE INDEX idx_cases_region ON cases(region_id);
CREATE INDEX idx_cases_reportdate ON cases(reportdate);
CREATE INDEX idx_cases_uuid ON cases(uuid);
```

#### Regions Table
```sql
CREATE TABLE region (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    changedate TIMESTAMP NOT NULL,
    creationdate TIMESTAMP NOT NULL,

    name VARCHAR(255) NOT NULL,
    epidcode VARCHAR(255),
    externald VARCHAR(255),
    archived BOOLEAN DEFAULT FALSE,

    country_id BIGINT REFERENCES country(id)
);
```

#### Population Data Table
```sql
CREATE TABLE populationdata (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    changedate TIMESTAMP NOT NULL,
    creationdate TIMESTAMP NOT NULL,

    region_id BIGINT REFERENCES region(id),
    district_id BIGINT REFERENCES district(id),

    agegroup VARCHAR(255),
    sex VARCHAR(10),
    population INTEGER,
    collectiondate DATE
);
```

### 4.2 AI Module Database Needs

**POC Phase:**
- **Read-only access** to existing tables
- No new tables required
- Queries: cases, region, populationdata

**Future Phase:**
- Dedicated schema: `ai_predictions`
- Tables:
  - `outbreak_predictions`
  - `model_versions`
  - `prediction_history`
  - `model_metrics`

**Example Future Schema:**
```sql
CREATE SCHEMA IF NOT EXISTS ai_predictions;

CREATE TABLE ai_predictions.outbreak_predictions (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    prediction_id VARCHAR(255) NOT NULL,

    region_id BIGINT REFERENCES region(id),
    disease VARCHAR(255) NOT NULL,

    risk_score DECIMAL(5, 4),
    confidence DECIMAL(5, 4),
    predicted_cases INTEGER,
    prediction_horizon INTEGER,

    model_version VARCHAR(100),
    prediction_timestamp TIMESTAMP NOT NULL,

    contributing_factors JSONB,
    recommendations TEXT
);

CREATE INDEX idx_predictions_region ON ai_predictions.outbreak_predictions(region_id);
CREATE INDEX idx_predictions_disease ON ai_predictions.outbreak_predictions(disease);
CREATE INDEX idx_predictions_timestamp ON ai_predictions.outbreak_predictions(prediction_timestamp);
```

---

## 5. Build System

### 5.1 Maven Configuration

#### Root POM (sormas-base)
```xml
<project>
    <groupId>de.symeda.sormas</groupId>
    <artifactId>sormas-base</artifactId>
    <version>1.103.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <vaadin8.version>8.14.3</vaadin8.version>
        <vaadin.version>24.3.0</vaadin.version>  <!-- NEW for Flow -->
        <hibernate.version>5.4.32.Final</hibernate.version>
        <slf4j.version>1.7.36</slf4j.version>
    </properties>

    <dependencyManagement>
        <!-- Version management for all modules -->
    </dependencyManagement>

    <modules>
        <module>../sormas-api</module>
        <module>../sormas-backend</module>
        <module>../sormas-ui</module>
        <module>../sormas-rest</module>
        <module>../sormas-ear</module>

        <!-- AI Modules -->
        <module>../sormas-ai-api</module>
        <module>../sormas-ai-backend</module>
        <module>../sormas-flow</module>
    </modules>
</project>
```

### 5.2 Build Commands

#### Full Build
```bash
cd /path/to/SORMAS-2.0/sormas-base
mvn clean install
```

#### Skip Tests
```bash
mvn clean install -DskipTests
```

#### Build Specific Module
```bash
cd sormas-ai-backend
mvn clean package
```

#### Build with Profiles
```bash
# Development profile
mvn clean install -Pdev

# Production profile
mvn clean install -Pprod
```

### 5.3 Build Output

```
sormas-base/target/               (no output, parent POM)
sormas-api/target/
  └── sormas-api-1.103.0-SNAPSHOT.jar
sormas-backend/target/
  └── sormas-backend-1.103.0-SNAPSHOT.jar
sormas-ai-api/target/
  └── sormas-ai-api-1.103.0-SNAPSHOT.jar
sormas-ai-backend/target/
  └── sormas-ai-backend-1.103.0-SNAPSHOT.jar
sormas-ui/target/
  └── sormas-ui-1.103.0-SNAPSHOT.war
sormas-flow/target/
  └── sormas-flow-1.103.0-SNAPSHOT.war
sormas-rest/target/
  └── sormas-rest-1.103.0-SNAPSHOT.war
sormas-ear/target/
  └── sormas-ear.ear                    # Main deployable
```

---

## 6. Deployment Model

### 6.1 Traditional Deployment

```
1. Build EAR
   $ cd sormas-base
   $ mvn clean install

2. Deploy to Payara
   $ cp sormas-ear/target/sormas-ear.ear \
        $PAYARA_HOME/glassfish/domains/domain1/autodeploy/

3. Monitor deployment
   $ tail -f $PAYARA_HOME/glassfish/domains/domain1/logs/server.log

4. Verify
   $ curl http://localhost:8080/sormas-ui
```

### 6.2 Docker Deployment (AI Services Only)

```yaml
# docker-compose.ai.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: sormas
      POSTGRES_USER: sormas
      POSTGRES_PASSWORD: sormas
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  sormas-ai:
    build: ./sormas-ai-models
    environment:
      DATABASE_URL: postgresql://sormas:sormas@postgres:5432/sormas
      MLFLOW_TRACKING_URI: http://mlflow:5000
    ports:
      - "8000:8000"
    depends_on:
      - postgres
      - mlflow

  mlflow:
    image: ghcr.io/mlflow/mlflow:v2.8.1
    command: >
      mlflow server
      --backend-store-uri postgresql://sormas:sormas@postgres:5432/mlflow
      --default-artifact-root /mlflow/artifacts
      --host 0.0.0.0
    ports:
      - "5000:5000"
    volumes:
      - mlflow-data:/mlflow

  redis:
    image: redis:7.2-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  postgres-data:
  mlflow-data:
  redis-data:

networks:
  default:
    name: sormas-ai-network
```

### 6.3 Hybrid Deployment (Recommended for POC)

```
┌─────────────────────────────────────────────┐
│  Docker Compose (AI Services)               │
│  - PostgreSQL                               │
│  - Python ML Service                        │
│  - MLflow                                   │
│  - Redis                                    │
└─────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────┐
│  Payara Server (Java Applications)          │
│  - sormas-ear.ear                           │
│    - sormas-backend.jar                     │
│    - sormas-ai-backend.jar ← NEW            │
│    - sormas-rest.war                        │
│    - sormas-ui.war                          │
│  - sormas-flow.war (separate) ← NEW         │
└─────────────────────────────────────────────┘
```

---

## 7. Integration Patterns

### 7.1 EJB Injection Pattern

**Used By:** sormas-ai-backend to access existing services

```java
package de.symeda.sormas.ai.backend;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless(name = "AIOutbreakPredictionFacade")
public class AIOutbreakPredictionFacadeEjb implements AIOutbreakPredictionFacade {

    // Inject existing SORMAS facades
    @EJB
    private CaseFacadeEjb caseFacade;

    @EJB
    private RegionFacadeEjb regionFacade;

    @EJB
    private PopulationDataFacadeEjb populationDataFacade;

    @Override
    public OutbreakPredictionDto predictOutbreak(
            RegionReferenceDto region,
            Disease disease,
            Integer predictionHorizon) {

        // 1. Get case data from existing facade
        CaseCriteria criteria = new CaseCriteria();
        criteria.region(region);
        criteria.disease(disease);

        List<CaseDataDto> cases = caseFacade.getIndexList(criteria, null, null, null);

        // 2. Get population data
        PopulationDataCriteria popCriteria = new PopulationDataCriteria();
        popCriteria.region(region);

        List<PopulationDataDto> popData = populationDataFacade.getPopulationData(popCriteria);

        // 3. Call AI model service
        // 4. Return prediction DTO
    }
}
```

### 7.2 HTTP Client Pattern

**Used By:** sormas-ai-backend to call Python ML service

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Stateless
public class AIModelService {

    private static final String AI_SERVICE_URL = "http://localhost:8000";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public PredictionResponse callPredictionAPI(PredictionRequest request) {
        String json = toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(AI_SERVICE_URL + "/predict/outbreak"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
            );

            return fromJson(response.body(), PredictionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI service call failed", e);
        }
    }
}
```

### 7.3 CDI Injection Pattern

**Used By:** sormas-flow for injecting facades

```java
package de.symeda.sormas.flow.views.ai;

import javax.inject.Inject;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "ai/outbreak-dashboard")
public class AIOutbreakDashboard extends VerticalLayout {

    @Inject
    private AIOutbreakPredictionFacade aiPredictionFacade;

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        loadPredictions();
    }

    private void loadPredictions() {
        List<OutbreakPredictionDto> predictions =
            aiPredictionFacade.getCurrentPredictions(Disease.CORONAVIRUS);

        updateUI(predictions);
    }
}
```

---

## 8. Security Model

### 8.1 Authentication

**Current System:**
- User/password authentication
- Session-based (JSESSIONID)
- Keycloak integration (optional)

**AI Module:**
- Inherits same authentication
- No separate login required
- Uses existing user context

### 8.2 Authorization

**Role-Based Access Control (RBAC):**
```java
public enum UserRole {
    ADMIN,
    NATIONAL_USER,
    SURVEILLANCE_SUPERVISOR,
    SURVEILLANCE_OFFICER,
    CONTACT_SUPERVISOR,
    CONTACT_OFFICER,
    // ... 20+ roles
}

public enum UserRight {
    CASE_VIEW,
    CASE_EDIT,
    CASE_DELETE,
    CONTACT_VIEW,
    // ... 100+ rights

    // NEW for AI
    AI_PREDICTION_VIEW,
    AI_MODEL_MANAGE
}
```

**Checking Permissions:**
```java
@RightsAllowed(UserRight.AI_PREDICTION_VIEW)
public List<OutbreakPredictionDto> getCurrentPredictions(Disease disease) {
    // Only users with AI_PREDICTION_VIEW can access
}
```

### 8.3 Data Access Control

**Region-Based Filtering:**
```java
// Users can only see data from their assigned regions
@Override
public List<OutbreakPredictionDto> getCurrentPredictions(Disease disease) {
    User currentUser = userService.getCurrentUser();
    Set<Region> userRegions = currentUser.getRegions();

    // Filter predictions by user's regions
    return predictions.stream()
        .filter(p -> userRegions.contains(p.getRegion()))
        .collect(Collectors.toList());
}
```

---

## 9. Key Interfaces and Contracts

### 9.1 Facade Pattern

All business logic exposed through facades:

```java
@Remote
public interface CaseFacade {
    CaseDataDto getCaseDataByUuid(String uuid);
    List<CaseDataDto> getIndexList(CaseCriteria criteria, ...);
    CaseDataDto saveCase(CaseDataDto dto);
    void deleteCase(String uuid);
    // ... 50+ methods
}
```

**AI follows same pattern:**
```java
@Remote
public interface AIOutbreakPredictionFacade {
    OutbreakPredictionDto predictOutbreak(...);
    List<OutbreakPredictionDto> getCurrentPredictions(...);
    List<OutbreakPredictionDto> getPredictionsByRiskLevel(...);
    String retrainModels();
}
```

### 9.2 DTO Pattern

All data transferred as DTOs:

```java
public class OutbreakPredictionDto extends EntityDto {
    private String predictionId;
    private RegionReferenceDto region;
    private Disease disease;
    private Double riskScore;
    private Double confidence;
    private Integer predictedCases;
    // ... getters/setters
}
```

---

## 10. Configuration Files

### 10.1 persistence.xml

```xml
<persistence version="2.1">
    <persistence-unit name="sormasPU" transaction-type="JTA">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <jta-data-source>jdbc/sormasDS</jta-data-source>

        <!-- Entities -->
        <class>de.symeda.sormas.backend.caze.Case</class>
        <class>de.symeda.sormas.backend.region.Region</class>
        <!-- ... 100+ entity classes -->

        <properties>
            <property name="hibernate.dialect"
                      value="de.symeda.sormas.backend.ExtendedPostgreSQL94Dialect"/>
        </properties>
    </persistence-unit>
</persistence>
```

### 10.2 sormas.properties

```properties
# Database
db.host=localhost
db.port=5432
db.name=sormas
db.user=sormas
db.password=sormas

# URLs
sormas.url=http://localhost:8080
sormas.path=/sormas-ui

# Feature flags
feature.outbreakPrediction=true
feature.aiDashboard=true

# AI Service
ai.service.url=http://localhost:8000
ai.service.timeout=30000
```

---

## Summary: Base System Integration Points

| Component | Integration Method | Impact on Base System |
|-----------|-------------------|----------------------|
| sormas-ai-api | Maven dependency | None (new module) |
| sormas-ai-backend | EJB injection | None (reads only) |
| sormas-flow | Separate deployment | None (coexists) |
| sormas-ai-models | HTTP client | None (external service) |

**Key Principle:** AI modules are **additive**, not **disruptive**. Existing SORMAS functionality remains unchanged.

---

**End of Part 2: Base System Architecture**

**Next Document**: PRD_03_AI_MODULES_TECHNICAL_SPECS.md
