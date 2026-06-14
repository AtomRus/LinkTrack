package backend.academy.linktracker.ai.grouping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.ai.dto.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.kafka.ProcessedUpdatePublisher;
import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateGroupingServiceTest {

    @Mock
    private ProcessedUpdatePublisher processedUpdatePublisher;

    private ScheduledExecutorService groupingScheduler;
    private UpdateGroupingService updateGroupingService;

    @BeforeEach
    void setUp() {
        AiAgentProperties properties = new AiAgentProperties();
        properties.getGrouping().setWindowMs(60_000);

        groupingScheduler = Executors.newSingleThreadScheduledExecutor();
        updateGroupingService = new UpdateGroupingService(processedUpdatePublisher, properties, groupingScheduler);
    }

    @AfterEach
    void tearDown() {
        groupingScheduler.shutdownNow();
    }

    @Test
    void shouldGroupMultipleUpdatesForSameChat() {
        updateGroupingService.enqueue(10L, 1L, "First update text", UpdatePriority.LOW);
        updateGroupingService.enqueue(10L, 2L, "Second update with critical issue", UpdatePriority.HIGH);

        updateGroupingService.flush(10L);

        ArgumentCaptor<ProcessedLinkUpdate> captor = ArgumentCaptor.forClass(ProcessedLinkUpdate.class);
        verify(processedUpdatePublisher).publish(captor.capture());

        ProcessedLinkUpdate published = captor.getValue();
        assertThat(published.id()).isEqualTo(1L);
        assertThat(published.tgChatIds()).containsExactly(10L);
        assertThat(published.priority()).isEqualTo(UpdatePriority.HIGH);
        assertThat(published.description()).isEqualTo("1. First update text\n2. Second update with critical issue");
    }

    @Test
    void shouldNotGroupSingleUpdate() {
        String description = "Single update without grouping";
        updateGroupingService.enqueue(20L, 5L, description, UpdatePriority.MEDIUM);

        updateGroupingService.flush(20L);

        verify(processedUpdatePublisher)
                .publish(argThat(update -> update.description().equals(description)
                        && update.priority() == UpdatePriority.MEDIUM
                        && update.tgChatIds().equals(java.util.List.of(20L))));
    }

    @Test
    void shouldMergeDescriptionsAsNumberedList() {
        assertThat(UpdateGroupingService.mergeDescriptions(java.util.List.of("only one")))
                .isEqualTo("only one");
        assertThat(UpdateGroupingService.mergeDescriptions(java.util.List.of("a", "b")))
                .isEqualTo("1. a\n2. b");
    }
}
