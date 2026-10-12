FROM golang:1.22-bookworm AS build
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends protobuf-compiler && rm -rf /var/lib/apt/lists/*
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.34.1 && \
    go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.4.0
ENV PATH="/root/go/bin:${PATH}"

COPY go.mod .
COPY main.go .
COPY proto ./proto
COPY Makefile .

RUN go mod tidy
RUN make proto
RUN go build -o /ingestion-service .

FROM debian:bookworm-slim
COPY --from=build /ingestion-service /usr/local/bin/ingestion-service
EXPOSE 8070
ENTRYPOINT ["ingestion-service"]
