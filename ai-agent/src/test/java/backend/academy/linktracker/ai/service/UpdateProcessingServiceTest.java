package backend.academy.linktracker.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.filter.UpdateFilter;
import backend.academy.linktracker.ai.grouping.UpdateGroupingService;
import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.prioritization.UpdatePrioritizer;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.summarization.StubSummarizer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProcessingServiceTest {

    @Mock
    private UpdateGroupingService updateGroupingService;

    private UpdateProcessingService updateProcessingService;
    private AiAgentProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AiAgentProperties();
        properties.getFiltering().setStopWords(List.of("spam"));
        properties.getFiltering().setExcludedAuthors(List.of("bot-user"));
        properties.getFiltering().setMinLength(20);
        properties.getSummarization().setThreshold(500);
        properties.getPrioritization().setHighKeywords(List.of("critical"));
        properties.getPrioritization().setLowKeywords(List.of("typo"));

        updateProcessingService = new UpdateProcessingService(
                new UpdateFilter(), new StubSummarizer(), new UpdatePrioritizer(), updateGroupingService, properties);
    }

    @Test
    void shouldNotEnqueueFilteredUpdate() {
        RawLinkUpdate update = new RawLinkUpdate(1L, "contains spam word here", "alice", List.of(1L));

        Optional<RawLinkUpdate> result = updateProcessingService.process(update);

        assertThat(result).isEmpty();
        verify(updateGroupingService, never())
                .enqueue(
                        org.mockito.Mockito.anyLong(),
                        org.mockito.Mockito.anyLong(),
                        org.mockito.Mockito.any(),
                        org.mockito.Mockito.any());
    }

    @Test
    void shouldEnqueueWithPrioritizedDescription() {
        RawLinkUpdate update = new RawLinkUpdate(42L, "critical issue in service", "alice", List.of(10L, 20L));

        Optional<RawLinkUpdate> result = updateProcessingService.process(update);

        assertThat(result).isPresent();
        verify(updateGroupingService).enqueue(10L, 42L, "critical issue in service", UpdatePriority.HIGH);
        verify(updateGroupingService).enqueue(20L, 42L, "critical issue in service", UpdatePriority.HIGH);
    }
}
