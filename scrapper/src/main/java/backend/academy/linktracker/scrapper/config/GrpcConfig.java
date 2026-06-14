package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.bot.grpc.UpdateServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Value("${GRPC_BOT_ADDRESS:0.0.0.0:9092}")
    private String botAddress;

    @Bean
    UpdateServiceGrpc.UpdateServiceBlockingStub stub(GrpcChannelFactory channelFactory) {
        return UpdateServiceGrpc.newBlockingStub(channelFactory.createChannel(botAddress));
    }
}
