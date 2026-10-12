# PulseTrace — Distributed Log & User-Analytics Pipeline

## Business problem

Engineering teams running several microservices often can't answer "is anything on fire right now?" quickly because logs and error rates are scattered across each service's own console/log files. PulseTrace centralizes log ingestion and gives a live, per-service error-rate view without needing to grep through individual service logs.

## Architecture

```
Any service --(HTTP)--> ingestion-service (Go) --(Kafka: raw-logs)--> aggregator-service (Java/Spring Boot)
                              ^                                              |
                              |___________________(gRPC: GetErrorRate)______|
                                                                              |
                                                                       MySQL (running counters)
                                                                              |
                                                                  BigQuery / local export sink
                                                                              |
                                                                       React dashboard
```

- **ingestion-service** (**Go**) exposes `POST /logs` and forwards each entry onto the `raw-logs` **Kafka** topic. It also runs a background loop that calls **aggregator-service over gRPC** every 30 seconds to self-monitor downstream error rates — a real polyglot gRPC call, not just REST between services.
- **aggregator-service** (**Java/Spring Boot**, built with **Maven**, gRPC server via `grpc-server-spring-boot-starter`) consumes `raw-logs`, maintains running request/error counters per service in **MySQL**, exposes them over both a REST API (for the dashboard) and the gRPC `LogStats` service (for ingestion-service's self-monitoring). Every 5 minutes it snapshots and resets those counters into an export sink — **BigQuery** in production (`app.bigquery.enabled=true`), or a local MySQL table by default so the whole thing runs without GCP credentials.
- **dashboard** (**React**) polls the aggregator's REST API and highlights services whose error rate is climbing.
- Both services are containerized with **Docker**, deployable to **GCP** (GKE/Cloud Run) or any **Kubernetes** cluster, on **Linux** base images throughout.

## Tech stack coverage

Go, Java, Spring Boot, gRPC, Microservices, Distributed Systems, Kafka, BigQuery (+ local fallback), MySQL, GCP-ready, Docker, Kubernetes-ready, Linux, Git, CI/CD-ready, automated testing (JUnit + Mockito), Observability (structured logs + the self-monitoring gRPC loop), React.

## Running it locally

```bash
docker compose up --build
```

- Dashboard: http://localhost:3002
- Send a test log: `curl -X POST localhost:8070/logs -d '{"service":"checkout-service","level":"ERROR","message":"payment timeout"}' -H 'Content-Type: application/json'`
- Watch `docker compose logs -f ingestion-service` to see the self-monitoring gRPC calls print live error rates.

The Go build compiles the gRPC stubs from `proto/logstats.proto` automatically inside the Docker build (see `ingestion-service/Dockerfile`); the Java side generates its stubs automatically via the `protobuf-maven-plugin` on `mvn package` — no manual codegen step needed for either.

## Running the tests

```bash
cd aggregator-service && mvn test
```

## Taking it further (what to mention in an interview)

- Replace the 5-minute reset-and-snapshot approach with real stream windowing (Kafka Streams or Flink) for accurate sliding windows instead of fixed resets.
- Add an alerting rule (e.g. via a scheduled job or Cloud Monitoring) that pages when a service's error rate crosses a threshold for two consecutive windows.
- Persist raw log lines (not just counters) to BigQuery/GCS for later ad-hoc querying, not just the aggregated rate.
- Add OpenTelemetry tracing across the HTTP → Kafka → gRPC hops to visualize end-to-end latency.
