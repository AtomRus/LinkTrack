package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.filter.UpdateFilter;
import backend.academy.linktracker.ai.grouping.UpdateGroupingService;
import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.prioritization.UpdatePrioritizer;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.summarization.Summarizer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProcessingService {

    private final UpdateFilter updateFilter;
    private final Summarizer summarizer;
    private final UpdatePrioritizer updatePrioritizer;
    private final UpdateGroupingService updateGroupingService;
    private final AiAgentProperties properties;

    public Optional<RawLinkUpdate> process(RawLinkUpdate update) {
        if (!updateFilter.passes(update, properties.getFiltering())) {
            log.debug("Обновление {} отфильтровано", update.id());
            return Optional.empty();
        }

        String description = summarizer.summarize(
                update.description(), properties.getSummarization().getThreshold());
        UpdatePriority priority = updatePrioritizer.prioritize(description, properties.getPrioritization());

        for (Long tgChatId : update.tgChatIds()) {
            updateGroupingService.enqueue(tgChatId, update.id(), description, priority);
        }
        return Optional.of(update);
    }
}
