package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.dto.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedUpdatePublisher {

    private final KafkaTemplate<Long, ProcessedLinkUpdate> processedUpdatesKafkaTemplate;
    private final AiAgentProperties properties;

    public void publish(ProcessedLinkUpdate update) {
        processedUpdatesKafkaTemplate.send(properties.getKafka().getProcessedTopic(), update.id(), update);
    }
}
