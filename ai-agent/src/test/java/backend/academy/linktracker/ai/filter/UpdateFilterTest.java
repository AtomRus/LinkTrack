package backend.academy.linktracker.ai.filter;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateFilterTest {

    private UpdateFilter updateFilter;
    private AiAgentProperties.Filtering filtering;

    @BeforeEach
    void setUp() {
        updateFilter = new UpdateFilter();
        filtering = new AiAgentProperties.Filtering();
        filtering.setStopWords(List.of("spam", "ads", "promo"));
        filtering.setExcludedAuthors(List.of("bot-user"));
        filtering.setMinLength(20);
    }

    @Test
    void shouldFilterByStopWord() {
        RawLinkUpdate update = new RawLinkUpdate(1L, "This message contains spam content here", "alice", List.of(1L));

        assertThat(updateFilter.passes(update, filtering)).isFalse();
    }

    @Test
    void shouldFilterByExcludedAuthor() {
        RawLinkUpdate update = new RawLinkUpdate(1L, "Valid description with enough length", "bot-user", List.of(1L));

        assertThat(updateFilter.passes(update, filtering)).isFalse();
    }

    @Test
    void shouldFilterByMinLength() {
        RawLinkUpdate update = new RawLinkUpdate(1L, "too short", "alice", List.of(1L));

        assertThat(updateFilter.passes(update, filtering)).isFalse();
    }

    @Test
    void shouldPassValidUpdate() {
        RawLinkUpdate update = new RawLinkUpdate(1L, "Valid update without blocked words", "alice", List.of(1L, 2L));

        assertThat(updateFilter.passes(update, filtering)).isTrue();
    }
}
