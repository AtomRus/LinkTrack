package backend.academy.linktracker.ai.kafka;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.service.UpdateProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class RawUpdatesKafkaListener {

    private final JsonMapper jsonMapper;
    private final UpdateProcessingService updateProcessingService;

    public RawUpdatesKafkaListener(JsonMapper jsonMapper, UpdateProcessingService updateProcessingService) {
        this.jsonMapper = jsonMapper;
        this.updateProcessingService = updateProcessingService;
    }

    @KafkaListener(
            topics = "${ai-agent.kafka.raw-topic:link.raw-updates}",
            groupId = "${ai-agent.kafka.group-id:ai-agent}",
            containerFactory = "rawUpdatesKafkaListenerContainerFactory")
    public void onMessage(@Payload String payload) {
        try {
            RawLinkUpdate update = jsonMapper.readValue(payload, RawLinkUpdate.class);
            updateProcessingService.process(update);
        } catch (Exception ex) {
            log.error("Не удалось обработать сырое обновление: {}", ex.getMessage());
        }
    }
}
