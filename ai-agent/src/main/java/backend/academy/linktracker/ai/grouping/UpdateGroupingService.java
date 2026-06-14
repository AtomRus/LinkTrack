package backend.academy.linktracker.ai.grouping;

import backend.academy.linktracker.ai.dto.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.kafka.ProcessedUpdatePublisher;
import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateGroupingService {

    private final ProcessedUpdatePublisher processedUpdatePublisher;
    private final AiAgentProperties properties;
    private final ScheduledExecutorService groupingScheduler;

    private final ConcurrentHashMap<Long, ChatBuffer> buffers = new ConcurrentHashMap<>();

    public void enqueue(long tgChatId, long updateId, String description, UpdatePriority priority) {
        buffers.compute(tgChatId, (chatId, buffer) -> {
            ChatBuffer activeBuffer = buffer;
            if (activeBuffer == null) {
                activeBuffer = new ChatBuffer();
                scheduleFlush(chatId, activeBuffer);
            }
            synchronized (activeBuffer) {
                activeBuffer.updates.add(new PendingChatUpdate(updateId, description, priority));
            }
            return activeBuffer;
        });
    }

    void flush(long tgChatId) {
        ChatBuffer buffer = buffers.remove(tgChatId);
        if (buffer == null) {
            return;
        }
        List<PendingChatUpdate> updates;
        synchronized (buffer) {
            if (buffer.updates.isEmpty()) {
                return;
            }
            updates = List.copyOf(buffer.updates);
            buffer.updates.clear();
        }
        cancelFlushTask(buffer);
        processedUpdatePublisher.publish(combine(tgChatId, updates));
    }

    private void scheduleFlush(long tgChatId, ChatBuffer buffer) {
        long windowMs = properties.getGrouping().getWindowMs();
        ScheduledFuture<?> task = groupingScheduler.schedule(() -> flush(tgChatId), windowMs, TimeUnit.MILLISECONDS);
        buffer.flushTask = task;
    }

    private static void cancelFlushTask(ChatBuffer buffer) {
        ScheduledFuture<?> task = buffer.flushTask;
        if (task != null) {
            task.cancel(false);
        }
    }

    static ProcessedLinkUpdate combine(long tgChatId, List<PendingChatUpdate> updates) {
        long id = updates.getFirst().id();
        UpdatePriority priority = updates.getFirst().priority();
        List<String> descriptions = new ArrayList<>();
        for (PendingChatUpdate update : updates) {
            priority = UpdatePriority.max(priority, update.priority());
            descriptions.add(update.description());
        }
        String description = mergeDescriptions(descriptions);
        return new ProcessedLinkUpdate(id, description, List.of(tgChatId), priority);
    }

    static String mergeDescriptions(List<String> descriptions) {
        if (descriptions.size() == 1) {
            return descriptions.getFirst();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < descriptions.size(); i++) {
            builder.append(i + 1).append(". ").append(descriptions.get(i));
            if (i < descriptions.size() - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private static final class ChatBuffer {
        private final List<PendingChatUpdate> updates = new ArrayList<>();
        private volatile ScheduledFuture<?> flushTask;
    }
}
