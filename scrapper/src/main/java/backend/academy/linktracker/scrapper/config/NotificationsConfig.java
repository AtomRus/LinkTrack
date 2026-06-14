package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.notification.GrpcNotificationSender;
import backend.academy.linktracker.scrapper.notification.HttpNotificationSender;
import backend.academy.linktracker.scrapper.notification.KafkaNotificationSender;
import backend.academy.linktracker.scrapper.notification.NotificationSender;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NotificationsConfig {

    @Bean
    @Primary
    public NotificationSender notificationSender(
            NotificationsProperties notificationsProperties,
            KafkaNotificationSender kafkaNotificationSender,
            GrpcNotificationSender grpcNotificationSender,
            HttpNotificationSender httpNotificationSender) {
        return switch (notificationsProperties.getTransport().trim().toLowerCase()) {
            case "kafka" -> kafkaNotificationSender;
            case "grpc" -> grpcNotificationSender;
            case "http" -> httpNotificationSender;
            default ->
                throw new IllegalArgumentException(
                        "Unknown app.notifications.transport: " + notificationsProperties.getTransport());
        };
    }
}
