package backend.academy.linktracker.loadservice.config;

import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Bean
    LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub(
            GrpcChannelFactory channelFactory, @Value("${load.grpc.scrapper-target}") String scrapperTarget) {
        return LinkServiceGrpc.newBlockingStub(channelFactory.createChannel(scrapperTarget));
    }
}
