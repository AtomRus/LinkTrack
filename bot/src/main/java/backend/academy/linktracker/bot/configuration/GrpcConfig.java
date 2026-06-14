package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Value("${GRPC_SCRAPPER_ADDRESS:0.0.0.0:9091}")
    private String SCRAPPER_LINK;

    @Bean
    LinkServiceGrpc.LinkServiceBlockingStub stub(GrpcChannelFactory channelFactory) {
        return LinkServiceGrpc.newBlockingStub(channelFactory.createChannel(SCRAPPER_LINK));
    }
}
