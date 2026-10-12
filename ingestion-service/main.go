package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"time"

	kafka "github.com/segmentio/kafka-go"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	pb "logpipe/ingestion/logstatspb"
)

// LogEntry is the payload accepted at POST /logs and forwarded verbatim
// (as JSON) onto the "raw-logs" Kafka topic for aggregator-service to
// consume.
type LogEntry struct {
	Service   string `json:"service"`
	Level     string `json:"level"`
	Message   string `json:"message"`
	Timestamp int64  `json:"timestamp"`
}

var kafkaWriter *kafka.Writer

func handleLogs(w http.ResponseWriter, r *http.Request) {
	var entry LogEntry
	if err := json.NewDecoder(r.Body).Decode(&entry); err != nil {
		http.Error(w, "invalid payload", http.StatusBadRequest)
		return
	}
	if entry.Timestamp == 0 {
		entry.Timestamp = time.Now().UnixMilli()
	}
	payload, err := json.Marshal(entry)
	if err != nil {
		http.Error(w, "failed to encode entry", http.StatusInternalServerError)
		return
	}
	if err := kafkaWriter.WriteMessages(r.Context(), kafka.Message{
		Key:   []byte(entry.Service),
		Value: payload,
	}); err != nil {
		http.Error(w, "failed to publish log", http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusAccepted)
}

// pollErrorRates is a background self-monitoring loop: the ingestion tier
// asks the aggregator (over gRPC) how error-prone each downstream service
// currently looks, so it could apply backpressure or alert if things spike.
func pollErrorRates(addr string, services []string) {
	conn, err := grpc.NewClient(addr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Printf("could not connect to aggregator gRPC service: %v", err)
		return
	}
	defer conn.Close()
	client := pb.NewLogStatsClient(conn)

	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		for _, svc := range services {
			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			resp, err := client.GetErrorRate(ctx, &pb.ErrorRateRequest{ServiceName: svc, WindowMinutes: 5})
			cancel()
			if err != nil {
				log.Printf("gRPC GetErrorRate(%s) failed: %v", svc, err)
				continue
			}
			log.Printf("[monitor] %s error rate (last 5m): %.2f%% (%d/%d requests)",
				svc, resp.ErrorRate*100, resp.ErrorCount, resp.TotalRequests)
		}
	}
}

func envOrDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func main() {
	brokers := envOrDefault("KAFKA_BROKER", "localhost:9092")
	aggregatorAddr := envOrDefault("AGGREGATOR_GRPC_ADDR", "localhost:9090")

	kafkaWriter = &kafka.Writer{
		Addr:     kafka.TCP(brokers),
		Topic:    "raw-logs",
		Balancer: &kafka.LeastBytes{},
	}
	defer kafkaWriter.Close()

	go pollErrorRates(aggregatorAddr, []string{"checkout-service", "payments-service", "search-service"})

	http.HandleFunc("/logs", handleLogs)
	http.HandleFunc("/healthz", func(w http.ResponseWriter, r *http.Request) { _, _ = w.Write([]byte("ok")) })

	log.Println("ingestion-service listening on :8070")
	log.Fatal(http.ListenAndServe(":8070", nil))
}
