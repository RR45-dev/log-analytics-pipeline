package com.logpipe.aggregator.grpc;

import com.logpipe.aggregator.model.ServiceStat;
import com.logpipe.aggregator.service.StatsService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Backs the LogStats gRPC service that ingestion-service (Go) polls to
 * self-monitor downstream error rates. Generated base class comes from
 * proto/logstats.proto via the protobuf-maven-plugin (see pom.xml) — no
 * hand-written stubs needed.
 */
@GrpcService
@RequiredArgsConstructor
public class LogStatsGrpcService extends LogStatsGrpc.LogStatsImplBase {

    private final StatsService statsService;

    @Override
    public void getErrorRate(ErrorRateRequest request, StreamObserver<ErrorRateResponse> responseObserver) {
        ServiceStat stat = statsService.getStat(request.getServiceName());
        double errorRate = stat.getTotalRequests() == 0 ? 0.0 : (double) stat.getErrorCount() / stat.getTotalRequests();

        ErrorRateResponse response = ErrorRateResponse.newBuilder()
                .setServiceName(stat.getServiceName())
                .setTotalRequests(stat.getTotalRequests())
                .setErrorCount(stat.getErrorCount())
                .setErrorRate(errorRate)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
