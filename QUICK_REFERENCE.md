# Quick Reference Guide - Observability Stack

## 📊 URLs Quick Access

| Service | URL | Username | Password |
|---------|-----|----------|----------|
| Grafana | http://localhost:3000 | admin | admin |
| Prometheus | http://localhost:9091 | - | - |
| Loki | http://localhost:3100 | - | - |
| AlertManager | http://localhost:9093 | - | - |
| Spring Boot App | http://localhost:8080 | - | - |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus | - | - |

---

## 🚀 Quick Start Commands

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f prometheus
docker-compose logs -f grafana
docker-compose logs -f loki

# Restart a service
docker-compose restart prometheus

# Stop and remove volumes
docker-compose down -v

# Rebuild and start
docker-compose up -d --build
```

### Kubernetes

```bash
# Deploy observability stack
kubectl apply -f k8s-observability-manifests.yaml

# Check status
kubectl get all -n observability

# View logs
kubectl logs -n observability -l app=prometheus

# Port forward to Grafana
kubectl port-forward -n observability svc/grafana 3000:3000

# Port forward to Prometheus
kubectl port-forward -n observability svc/prometheus 9091:9090

# Delete all resources
kubectl delete -f k8s-observability-manifests.yaml
```

---

## 📈 Common Prometheus Queries

### HTTP Metrics

```promql
# Request rate (requests per second)
rate(http_server_requests_seconds_count[5m])

# Error rate (5xx errors)
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Client error rate (4xx errors)
rate(http_server_requests_seconds_count{status=~"4.."}[5m])

# Success rate percentage
(sum(rate(http_server_requests_seconds_count{status!~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))) * 100

# Response time p50 (median)
histogram_quantile(0.50, rate(http_server_requests_seconds_bucket[5m]))

# Response time p95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Response time p99
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))

# Average response time
sum(rate(http_server_requests_seconds_sum[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

### JVM Metrics

```promql
# Heap memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Non-heap memory usage
jvm_memory_used_bytes{area="nonheap"}

# GC time per second
rate(jvm_gc_pause_seconds_sum[5m])

# Young generation GC time
rate(jvm_gc_pause_seconds_sum{action="end of minor GC"}[5m])

# Old generation GC time
rate(jvm_gc_pause_seconds_sum{action="end of major GC"}[5m])

# Thread count
jvm_threads_live_threads

# Peak thread count
jvm_threads_peak_threads

# Thread utilization
(jvm_threads_live_threads / jvm_threads_peak_threads) * 100
```

### Process Metrics

```promql
# CPU usage percentage
process_cpu_usage * 100

# Resident memory (in MB)
process_resident_memory_bytes / 1024 / 1024

# File descriptors usage
process_open_fds / process_max_fds * 100

# Uptime in seconds
process_uptime_seconds
```

### Database Metrics

```promql
# Active connections
hikaricp_connections_active

# Total connections
hikaricp_connections

# Connection pool utilization
(hikaricp_connections_active / hikaricp_connections) * 100

# Available connections
hikaricp_connections_available

# Connection wait time (if available)
rate(hikaricp_connection_timeout_total[5m])
```

### Application Metrics

```promql
# Custom API calls total
increase(api_calls_total[5m])

# Custom API errors
increase(api_errors_total[5m])

# Database query time average
sum(rate(database_query_time_sum[5m])) / sum(rate(database_query_time_count[5m]))
```

---

## 🔍 Common Loki Queries

### Basic Queries

```logql
# All logs from a specific service
{service="spring-boot-app"}

# Logs from specific container
{container="app"}

# Logs from specific namespace
{namespace="observability"}

# All ERROR logs
{service="spring-boot-app"} | level=ERROR

# All WARNING logs
{service="spring-boot-app"} | level=WARN

# Search for specific message
{service="spring-boot-app"} | "NullPointerException"
```

### Advanced Queries

```logql
# Error count in last hour
count_over_time({service="spring-boot-app"} | level=ERROR [1h])

# Parse JSON logs and filter
{service="spring-boot-app"} | json | statusCode >= 500

# Regex pattern matching
{service="spring-boot-app"} | regexp "error|exception"

# Count logs by logger
{service="spring-boot-app"} | json | stats count() by logger

# Response time extraction and stats
{service="spring-boot-app"} | json | stats avg(duration_ms), min(duration_ms), max(duration_ms) by path

# Rate of errors per second
rate({service="spring-boot-app"} | level=ERROR [1m])
```

---

## ⚠️ Common Alert Queries

```promql
# Application is down
up{job="spring-boot-app"} == 0

# High memory usage (>85%)
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85

# High error rate (>5%)
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05

# High response time (p95 > 1 second)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1

# Low disk space (<15%)
node_filesystem_avail_bytes / node_filesystem_size_bytes < 0.15

# High thread count (>100)
jvm_threads_live_threads > 100

# Low database connections (<5)
hikaricp_connections_available < 5

# High database connection usage (>80%)
hikaricp_connections_active / hikaricp_connections > 0.8
```

---

## 📝 Configuration Files Summary

| File | Purpose | Location |
|------|---------|----------|
| prometheus.yml | Prometheus configuration | prometheus/ |
| alert_rules.yml | Alert rules | prometheus/ |
| recording_rules.yml | Recording rules for optimization | prometheus/ |
| loki-config.yml | Loki configuration | loki/ |
| promtail-config.yml | Log shipping configuration | promtail/ |
| alertmanager.yml | Alert routing and notifications | alertmanager/ |
| application.yml | Spring Boot configuration | app/ |
| docker-compose.yml | Local setup | root |

---

## 🔧 Troubleshooting Quick Fixes

### Prometheus not scraping

```bash
# Check if Spring Boot app is healthy
curl http://localhost:8080/api/health

# Verify metrics endpoint
curl http://localhost:8080/actuator/prometheus | head -20

# Check Prometheus targets
curl http://localhost:9091/api/v1/targets | jq '.data.activeTargets'

# Check Prometheus alerts
curl http://localhost:9091/api/v1/alerts
```

### Loki not receiving logs

```bash
# Check Promtail status
docker-compose logs promtail | grep -i error

# Verify Loki connectivity
curl http://localhost:3100/loki/api/v1/status

# Check Promtail config
docker exec promtail cat /etc/promtail/config.yml
```

### Grafana datasource issues

```bash
# Test Prometheus datasource
curl -H "Authorization: Bearer <token>" http://localhost:3000/api/datasources/proxy/1/api/v1/query?query=up

# Test Loki datasource
curl http://localhost:3100/loki/api/v1/labels
```

---

## 📊 Key Metrics to Monitor

### Tier 1 - Critical
- Application availability (uptime)
- Error rate
- Response time (p99)
- JVM heap memory

### Tier 2 - Important
- Request rate
- CPU usage
- Database connection pool
- GC pause time

### Tier 3 - Nice to have
- Thread count
- File descriptors
- Network I/O
- Custom business metrics

---

## 🔐 Security Checklist

- [ ] Change default Grafana password
- [ ] Implement RBAC for Kubernetes resources
- [ ] Enable authentication for Prometheus (reverse proxy)
- [ ] Use TLS/HTTPS for all external communication
- [ ] Restrict network access to observability services
- [ ] Implement secrets management for sensitive configs
- [ ] Enable audit logging
- [ ] Set up data retention policies

---

## 📈 Optimization Tips

1. **Reduce Cardinality**: Use relabeling to drop unnecessary labels
2. **Recording Rules**: Pre-compute expensive queries (see recording-rules.yml)
3. **Retention**: Set appropriate retention periods to save storage
4. **Sampling**: Use log sampling to reduce ingestion rate
5. **Caching**: Enable caching in Grafana for frequently used dashboards

---

## 🚀 Performance Guidelines

| Component | CPU | Memory | Disk | Notes |
|-----------|-----|--------|------|-------|
| Prometheus | 0.25 - 1 CPU | 512MB - 2GB | 50GB - 500GB | Depends on metrics volume |
| Loki | 0.1 - 0.5 CPU | 256MB - 1GB | 50GB - 500GB | Depends on log volume |
| Grafana | 0.1 - 0.5 CPU | 128MB - 512MB | 10GB | Lightweight |
| Promtail | 0.05 - 0.1 CPU | 32MB - 128MB | 1GB | DaemonSet, light footprint |
| App (Spring) | 0.25 - 1 CPU | 512MB - 2GB | varies | Depends on application |

---

## 📚 Useful Links

- [Prometheus Docs](https://prometheus.io/docs/)
- [Grafana Docs](https://grafana.com/docs/)
- [Loki Docs](https://grafana.com/docs/loki/)
- [Micrometer](https://micrometer.io/)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Kubernetes Monitoring](https://kubernetes.io/docs/tasks/debug-application-cluster/resource-metrics-pipeline/)

---

## 💡 Pro Tips

1. Use dashboard templates from Grafana marketplace
2. Create custom alerts for business-critical metrics
3. Use log patterns to identify common issues
4. Implement SLI/SLO dashboards
5. Regular backup of Grafana dashboards and datasources
6. Use Prometheus relabeling to add context to metrics
7. Implement distributed tracing (optional - with Jaeger/Zipkin)
8. Monitor the monitoring stack itself

---

## 🔄 Maintenance Tasks

**Daily**:
- Check alert status
- Monitor disk usage
- Review error logs

**Weekly**:
- Review alert thresholds
- Check retention policies
- Update dashboards as needed

**Monthly**:
- Backup Grafana configuration
- Review and optimize queries
- Clean up old data
- Update dependencies

**Quarterly**:
- Capacity planning
- Performance optimization
- Security audit
- Documentation review
