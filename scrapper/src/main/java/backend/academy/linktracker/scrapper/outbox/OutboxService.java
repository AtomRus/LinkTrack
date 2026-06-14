package backend.academy.linktracker.scrapper.outbox;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OutboxEventEntity enqueue(KafkaEvent update) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setLinkId(update.getId());
        entity.setUrl(update.getUrl());
        entity.setDescription(update.getDescription());
        entity.setAuthor(update.getAuthor());
        entity.setStatus(OutboxStatus.NEW);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setTgChatIdsJson(toJson(update.getTgChatIds()));
        return outboxRepository.save(entity);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
