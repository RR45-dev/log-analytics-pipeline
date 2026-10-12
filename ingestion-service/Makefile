.PHONY: proto build run

# Requires: protoc, protoc-gen-go, protoc-gen-go-grpc on PATH.
# (Handled automatically inside the Docker build — see Dockerfile.)
proto:
	protoc -I proto \
		--go_out=. --go_opt=module=logpipe/ingestion \
		--go-grpc_out=. --go-grpc_opt=module=logpipe/ingestion \
		proto/logstats.proto

build: proto
	go build -o bin/ingestion-service .

run: build
	./bin/ingestion-service
