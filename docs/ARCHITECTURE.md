# Architecture

`ingestion-service` (Go) receives raw log events and publishes them to Kafka.
`aggregator-service` (Java) consumes those events, rolls them up into
per-service stats in MySQL, and exports rollups to BigQuery. The `dashboard`
(React) reads aggregated stats via the aggregator's REST/gRPC API.
