# SORMAS AI Deployment Guide

## Overview

This guide covers deploying the AI-enhanced SORMAS application to a server for testing and production use.

## Branch Information

**Branch Name**: `feature/ai-outbreak-prediction`

This branch contains all AI modules and enhancements to the SORMAS system.

## Quick Server Deployment

### Prerequisites

Ensure your server has:
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Git**: For cloning the repository
- **Ports Available**: 5432 (PostgreSQL), 8000 (AI Service), 5000 (MLflow), 6379 (Redis)

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone https://github.com/danieltomabba/SORMAS-2.0.git
cd SORMAS-2.0

# Checkout the AI feature branch
git checkout feature/ai-outbreak-prediction
```

### Step 2: Configure Environment Variables

```bash
# Create environment file for AI models
cat > sormas-ai-models/.env << EOF
DATABASE_URL=postgresql://sormas:sormas@postgres:5432/sormas
MLFLOW_TRACKING_URI=http://mlflow:5000
MODEL_PATH=/app/models/trained
MODEL_VERSION=v1.0.0-poc
LOG_LEVEL=INFO
API_HOST=0.0.0.0
API_PORT=8000
EOF
```

### Step 3: Start AI Services

```bash
# Make the startup script executable
chmod +x start-ai-services.sh

# Start all AI services
./start-ai-services.sh
```

This will start:
- PostgreSQL database
- AI Service (FastAPI)
- MLflow tracking server
- Redis cache

### Step 4: Verify Services

```bash
# Check if all containers are running
docker-compose -f docker-compose.ai.yml ps

# Test AI Service health
curl http://localhost:8000/health

# Test MLflow UI (should return HTML)
curl http://localhost:5000
```

Expected output:
```json
{
  "status": "healthy",
  "service": "SORMAS AI Service",
  "version": "v1.0.0-poc"
}
```

### Step 5: Build SORMAS Application

```bash
# Navigate to sormas-base directory
cd sormas-base

# Build all modules including AI
mvn clean install -DskipTests

# Or build with tests
mvn clean install
```

### Step 6: Build Flow Module

```bash
# Navigate to Flow module
cd ../sormas-flow

# Install frontend dependencies
npm install

# Build the Flow web application
mvn clean package
```

### Step 7: Deploy to Payara Server

If you have Payara Server installed:

```bash
# Copy EAR file to Payara autodeploy
cp sormas-ear/target/sormas-ear.ear $PAYARA_HOME/glassfish/domains/domain1/autodeploy/

# Copy Flow WAR file
cp sormas-flow/target/sormas-flow.war $PAYARA_HOME/glassfish/domains/domain1/autodeploy/

# Start Payara if not running
$PAYARA_HOME/bin/asadmin start-domain
```

## Alternative: Docker-Based Full Deployment

If you want to containerize the entire SORMAS application:

### Create Full Docker Compose

Create `docker-compose.full.yml`:

```yaml
version: '3.8'

services:
  # Extend the AI services
  postgres:
    extends:
      file: docker-compose.ai.yml
      service: postgres

  sormas-ai:
    extends:
      file: docker-compose.ai.yml
      service: sormas-ai

  mlflow:
    extends:
      file: docker-compose.ai.yml
      service: mlflow

  redis:
    extends:
      file: docker-compose.ai.yml
      service: redis

  # Add Payara server
  sormas-app:
    image: payara/server-full:5.2022.5-jdk11
    container_name: sormas-app
    ports:
      - "6080:8080"
      - "4848:4848"
    volumes:
      - ./sormas-ear/target/sormas-ear.ear:/opt/payara/deployments/sormas-ear.ear
      - ./sormas-flow/target/sormas-flow.war:/opt/payara/deployments/sormas-flow.war
    environment:
      - JVM_ARGS=-Xmx2g -Xms512m
    depends_on:
      - postgres
      - sormas-ai
    networks:
      - sormas-ai-network

networks:
  sormas-ai-network:
    driver: bridge
```

Start everything:

```bash
docker-compose -f docker-compose.full.yml up -d
```

## Accessing the Application

After deployment:

1. **AI Dashboard**: http://your-server:6080/sormas-flow/ai/outbreak-dashboard
2. **AI Service API**: http://your-server:8000/docs
3. **MLflow UI**: http://your-server:5000

## Testing the AI Features

### Test Outbreak Prediction API

```bash
curl -X POST http://localhost:8000/predict/outbreak \
  -H "Content-Type: application/json" \
  -d '{
    "case_data": {
      "region_id": "test-region",
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

### Test Risk Assessment

```bash
curl -X POST http://localhost:8000/assess/risk \
  -H "Content-Type: application/json" \
  -d '{
    "region_id": "test-region",
    "disease": "CORONAVIRUS",
    "recent_cases": 150,
    "previous_cases": 100,
    "population_density": 1500
  }'
```

## Production Considerations

### Security

1. **Change default passwords** in `docker-compose.ai.yml`:
   - PostgreSQL password
   - MLflow authentication

2. **Enable HTTPS**:
   - Configure SSL certificates
   - Use a reverse proxy (nginx/traefik)

3. **Network security**:
   - Restrict port access using firewall
   - Use internal networks for service communication

### Performance

1. **Database optimization**:
   - Configure PostgreSQL for production workload
   - Set appropriate connection pool sizes
   - Enable query optimization

2. **AI Service scaling**:
   - Use multiple replicas behind a load balancer
   - Configure Gunicorn workers based on CPU cores
   - Add Redis for caching predictions

3. **Resource allocation**:
   ```yaml
   sormas-ai:
     deploy:
       resources:
         limits:
           cpus: '2'
           memory: 4G
         reservations:
           cpus: '1'
           memory: 2G
   ```

### Monitoring

1. **Health checks**:
   ```bash
   # Add to your monitoring system
   curl http://localhost:8000/health
   curl http://localhost:5000/health
   ```

2. **Logs**:
   ```bash
   # View AI service logs
   docker-compose logs -f sormas-ai

   # View all logs
   docker-compose logs -f
   ```

3. **Metrics**:
   - Access Prometheus metrics: http://localhost:8000/metrics
   - View MLflow experiments: http://localhost:5000

### Backup

1. **Database backup**:
   ```bash
   # Backup PostgreSQL data
   docker exec sormas-postgres pg_dump -U sormas sormas > backup.sql
   ```

2. **Model backup**:
   ```bash
   # Backup trained models
   docker cp sormas-ai:/app/models/trained ./models-backup
   ```

3. **MLflow artifacts**:
   ```bash
   # Backup MLflow tracking data
   cp -r mlflow/artifacts ./mlflow-backup
   ```

## Troubleshooting

### Services Not Starting

```bash
# Check Docker daemon
docker info

# Check logs for specific service
docker-compose logs sormas-ai

# Restart services
docker-compose down
docker-compose up -d
```

### Connection Issues

```bash
# Test database connection
docker exec sormas-postgres psql -U sormas -d sormas -c "SELECT 1"

# Test AI service connectivity
docker exec sormas-ai curl localhost:8000/health

# Check network
docker network inspect sormas-ai-network
```

### Build Failures

```bash
# Clean Maven cache
mvn clean

# Build with debug output
mvn clean install -X

# Check Java version
java -version  # Should be JDK 11+

# Check Maven version
mvn -version   # Should be 3.6.3+
```

### AI Predictions Failing

```bash
# Check AI service logs
docker logs sormas-ai

# Verify model files exist
docker exec sormas-ai ls -la /app/models/

# Test prediction endpoint directly
curl -v http://localhost:8000/predict/outbreak -d '{...}'
```

## Rollback Procedure

If you need to rollback:

```bash
# Stop AI services
docker-compose -f docker-compose.ai.yml down

# Switch to previous branch
git checkout main

# Rebuild if necessary
cd sormas-base
mvn clean install
```

## Support and Documentation

- **Full Implementation Guide**: See `README_AI_IMPLEMENTATION.md`
- **API Documentation**: http://localhost:8000/docs (when service is running)
- **GitHub Issues**: https://github.com/danieltomabba/SORMAS-2.0/issues

## Version Information

- **SORMAS Version**: 1.103.0-SNAPSHOT
- **AI Feature Version**: 1.0.0-POC
- **Branch**: feature/ai-outbreak-prediction
- **Last Updated**: 2025-10-17

---

**Note**: This is a proof-of-concept implementation. For production deployment, additional security hardening, performance optimization, and monitoring should be implemented.
