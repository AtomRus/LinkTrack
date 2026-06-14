package backend.academy.linktracker.scrapper.metrics;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public class GrpcApiMetricsInterceptor implements ServerInterceptor {

    private final OperationMetrics operationMetrics;

    public GrpcApiMetricsInterceptor(OperationMetrics operationMetrics) {
        this.operationMetrics = operationMetrics;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        operationMetrics.incrementApiRequests("grpc");
        return next.startCall(call, headers);
    }
}
