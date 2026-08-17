# Complete Observability Stack Setup Guide
## Spring Boot + Prometheus + Loki + Grafana + Alertmanager

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Local Setup with Docker Compose](#local-setup-with-docker-compose)
3. [Kubernetes Deployment](#kubernetes-deployment)
4. [Configuration Details](#configuration-details)
5. [Monitoring & Dashboards](#monitoring--dashboards)
6. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Your Application                         │
│                    (Spring Boot + Micrometer)                    │
└────────────┬──────────────────────┬──────────────────────┬──────┘
             │                      │                      │
             ▼                      ▼                      ▼
      ┌─────────────┐       ┌──────────────┐      ┌──────────────┐
      │ Metrics     │       │ Application  │      │ Container    │
      │ Endpoint    │       │ Logs         │      │ Logs         │
      │ /actuator/  │       │ (stdout/file)│      │              │
      │ prometheus  │       └──────┬───────┘      └──────┬───────┘
      └─────┬───────┘              │                     │
            │                      ▼                     ▼
            │               ┌─────────────┐      ┌─────────────┐
            │               │  Promtail   │      │  Promtail   │
            │               │ (log agent) │      │ (DaemonSet) │
            │               └──────┬──────┘      └──────┬──────┘
            │                      │                     │
            │                      ▼                     ▼
            │               ┌─────────────────────────────┐
            │               │        Loki                 │
            │               │  (Log Aggregation)          │
            │               └─────────┬───────────────────┘
            │                         │
            ▼                         ▼
      ┌──────────────────────────────────────┐
      │        Prometheus                    │
      │  (Metrics Collection & Storage)      │
      │                                      │
      │  - Scrapes metrics every 15s         │
      │  - Stores in TSDB                    │
      │  - Evaluates alert rules             │
      └────────────┬──────────────────────┬──┘
                   │                      │
                   ▼                      ▼
            ┌──────────────┐      ┌────────────────┐
            │   Grafana    │      │  Alertmanager  │
            │              │      │                │
            │ Dashboards   │      │ - Alert Router │
            │ Alerts       │      │ - Notifications│
            └──────────────┘      └────────────────┘
```

---

## Local Setup with Docker Compose

### 1. Directory Structure

```
observability-stack/
├── docker-compose.yml
├── prometheus/
│   ├── prometheus.yml
│   ├── alert_rules.yml
│   └── recording_rules.yml
├── loki/
│   └── loki-config.yml
├── promtail/
│   └── promtail-config.yml
├── alertmanager/
│   └── alertmanager.yml
├── grafana/
│   └── provisioning/
│       ├── datasources/
│       │   └── datasources.yml
│       └── dashboards/
│           └── dashboards.yml
├── app/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
└── logs/
```

### 2. Step-by-Step Setup

#### Step 1: Create Directory Structure

```bash
mkdir -p observability-stack/{prometheus,loki,promtail,alertmanager,grafana/provisioning/{datasources,dashboards},app/src,logs}
cd observability-stack
```

#### Step 2: Copy Configuration Files

Copy all the YAML and configuration files into their respective directories:

```bash
# Copy Prometheus config
cp prometheus-stack-values.yaml.yml prometheus/prometheus-stack-values.yaml.yml
cp alert-rules.yml prometheus/
cp recording_rules.yml prometheus/

# Copy Loki config
cp loki-config.yml loki/

# Copy Promtail config
cp promtail-config.yml promtail/

# Copy Alertmanager config
cp alertmanager.yml alertmanager/alertmanager.yml

# Copy Spring Boot files
cp pom.xml app/
cp Dockerfile app/
cp SpringBootObservabilityExample.java app/src/
cp spring-boot-application.yml app/
```

#### Step 3: Create Grafana Datasources Configuration

```bash
cat > grafana/provisioning/datasources/datasources.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: true
EOF
```

#### Step 4: Start the Stack

```bash
# Build and start all services
docker-compose up -d

# Check if all services are running
docker-compose ps

# View logs
docker-compose logs -f
```

#### Step 5: Access the Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | admin/admin |
| Prometheus | http://localhost:9091 | - |
| Loki | http://localhost:3100 | - |
| Spring Boot App | http://localhost:8080 | - |
| Metrics | http://localhost:8080/actuator/prometheus | - |
| Alertmanager | http://localhost:9093 | - |

### 3. Health Check

```bash
# Check application health
curl http://localhost:8080/api/health

# Check Prometheus targets
curl http://localhost:9091/api/v1/targets

# Check Prometheus alerts
curl http://localhost:9091/api/v1/rules

# Check Loki status
curl http://localhost:3100/loki/api/v1/status

# Check available metrics
curl http://localhost:8080/actuator/prometheus | head -50
```

---

## Kubernetes Deployment

### 1. Create Namespace and Deploy

```bash
# Apply all manifests
kubectl apply -f k8s-observability-manifests.yaml

# Verify deployment
kubectl get all -n observability

# Check pod status
kubectl get pods -n observability -w
```

### 2. Port Forwarding for Local Access

```bash
# Forward Grafana
kubectl port-forward -n observability svc/grafana 3000:3000

# Forward Prometheus
kubectl port-forward -n observability svc/prometheus 9091:9090

# Forward Loki
kubectl port-forward -n observability svc/loki 3100:3100
```

### 3. Deploy Spring Boot Application

```bash
cat > k8s-spring-boot-app.yaml << 'EOF'
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-boot-app
  namespace: observability
spec:
  replicas: 2
  selector:
    matchLabels:
      app: spring-boot-app
  template:
    metadata:
      labels:
        app: spring-boot-app
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "9090"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: app
        image: your-registry/microservice-app:1.0.0
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 9090
          name: prometheus
        env:
        - name: MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE
          value: "health,metrics,prometheus"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi

---
apiVersion: v1
kind: Service
metadata:
  name: spring-boot-app
  namespace: observability
spec:
  selector:
    app: spring-boot-app
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  - name: prometheus
    port: 9090
    targetPort: 9090
  type: LoadBalancer
EOF

kubectl apply -f k8s-spring-boot-app.yaml
```

---

## Configuration Details

### Spring Boot Configuration (application.yml)

Key configurations for observability:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 1m
    
    distribution:
      slo:
        http.server.requests: 50ms,100ms,200ms,500ms,1s
```

### Prometheus Scrape Configuration

- **Interval**: 15 seconds (configurable)
- **Timeout**: 10 seconds
- **Retention**: 30 days
- **Storage**: ~5GB per day (depending on metrics volume)

### Loki Log Configuration

- **Retention**: 30 days
- **Max chunk age**: 1 hour
- **Ingestion rate**: 100 MB/min
- **Storage**: ~500MB-1GB per day (depending on log volume)

### Alert Rules

Common alerts configured:
- High JVM memory usage (>85%, >95%)
- High error rate (5xx errors)
- High response time (>1s for p95)
- Application down
- High database connection usage
- Low disk space
- High garbage collection time

---

## Monitoring & Dashboards

### Creating a Custom Dashboard in Grafana

1. **Login to Grafana**: http://localhost:3000 (admin/admin)

2. **Create Dashboard**:
   - Click "Create" → "Dashboard"
   - Add panels using Prometheus and Loki data sources

3. **Example Prometheus Queries**:

```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Response time (p95)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# JVM memory usage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# CPU usage
process_cpu_usage

# Thread count
jvm_threads_live_threads
```

4. **Example Loki Queries**:

```logql
# All logs from app
{service="spring-boot-app"}

# Error logs only
{service="spring-boot-app"} | level=ERROR

# Specific logger
{service="spring-boot-app", logger="com.example.UserService"}

# Time range
{service="spring-boot-app"} | timestamp > 1h
```

### Pre-configured Dashboard Templates

Grafana provides templates for:
- JVM Monitoring
- Spring Boot Application
- Kubernetes monitoring
- Docker container monitoring

Search for them in "Dashboards" → "Manage" → Import

---

## Recording Rules

For better performance, create recording rules in Prometheus:

```yaml
# prometheus/recording_rules.yml
groups:
  - name: recording_rules
    interval: 15s
    rules:
      # HTTP metrics
      - record: job:http:request:rate5m
        expr: rate(http_server_requests_seconds_count[5m])
      
      # JVM metrics
      - record: job:jvm:memory:usage:percent
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes * 100
```

---

## Troubleshooting

### 1. Prometheus not scraping metrics

```bash
# Check Prometheus UI
curl http://localhost:9091/api/v1/targets

# Check if Spring Boot app is exposing metrics
curl http://localhost:8080/actuator/prometheus

# Check Prometheus logs
docker-compose logs prometheus
```

### 2. Logs not appearing in Loki

```bash
# Check Promtail logs
docker-compose logs promtail

# Check if containers are being picked up
docker-compose logs promtail | grep "discovered"

# Verify Loki is running
curl http://localhost:3100/loki/api/v1/status
```

### 3. High memory usage

- Increase retention period cautiously
- Use sampling for high-volume metrics
- Archive old data to cheaper storage

### 4. Alerts not firing

```bash
# Check alert rules
curl http://localhost:9091/api/v1/rules

# Check alert status
curl http://localhost:9091/api/v1/alerts

# Check Alertmanager config
docker-compose logs alertmanager
```

---

## Production Best Practices

1. **Storage**:
   - Use persistent volumes for Prometheus and Loki
   - Set up regular backups
   - Monitor storage usage

2. **High Availability**:
   - Run multiple Prometheus replicas with remote storage
   - Use Loki in distributed mode with object storage (S3, GCS)
   - Set up Grafana in HA mode

3. **Security**:
   - Enable authentication for Grafana
   - Use HTTPS/TLS for all connections
   - Implement network policies in Kubernetes
   - Use secrets for sensitive configuration

4. **Performance**:
   - Use service discovery for dynamic scraping
   - Implement metric relabeling to reduce cardinality
   - Use recording rules for complex queries
   - Set appropriate retention periods

5. **Alerting**:
   - Integrate with incident management systems (PagerDuty)
   - Configure multiple notification channels
   - Regularly test alert configurations
   - Document runbooks for alerts

---

## Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Loki Documentation](https://grafana.com/docs/loki/)
- [Micrometer Documentation](https://micrometer.io/docs/installing)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

---

## Support & Next Steps

1. Start with Docker Compose locally
2. Test with sample data and alerts
3. Migrate to Kubernetes with persistent storage
4. Configure production-grade alerting
5. Implement custom dashboards
6. Set up automated backups
