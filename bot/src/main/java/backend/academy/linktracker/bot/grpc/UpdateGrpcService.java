package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.mapper.LinkUpdateMapper;
import backend.academy.linktracker.bot.service.LinkUpdateHandler;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@GrpcService
public class UpdateGrpcService extends UpdateServiceGrpc.UpdateServiceImplBase {
    private final LinkUpdateHandler linkUpdateHandler;
    private final LinkUpdateMapper linkUpdateMapper;

    @Override
    public void sendUpdate(LinkUpdate request, StreamObserver<Empty> responseObserver) {
        linkUpdateHandler.handleModel(linkUpdateMapper.toLinkUpdateModel(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
