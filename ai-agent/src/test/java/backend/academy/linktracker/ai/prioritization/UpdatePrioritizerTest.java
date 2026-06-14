package backend.academy.linktracker.ai.prioritization;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdatePrioritizerTest {

    private UpdatePrioritizer updatePrioritizer;
    private AiAgentProperties.Prioritization prioritization;

    @BeforeEach
    void setUp() {
        updatePrioritizer = new UpdatePrioritizer();
        prioritization = new AiAgentProperties.Prioritization();
        prioritization.setHighKeywords(List.of("critical", "urgent", "breaking", "security"));
        prioritization.setLowKeywords(List.of("minor", "typo", "chore", "docs"));
    }

    @Test
    void shouldAssignHighPriorityWhenHighKeywordPresent() {
        UpdatePriority priority = updatePrioritizer.prioritize("critical bug fix in production", prioritization);

        assertThat(priority).isEqualTo(UpdatePriority.HIGH);
    }

    @Test
    void shouldAssignMediumPriorityWhenNoKeywordsPresent() {
        UpdatePriority priority = updatePrioritizer.prioritize("Regular update without special words", prioritization);

        assertThat(priority).isEqualTo(UpdatePriority.MEDIUM);
    }

    @Test
    void shouldAssignLowPriorityWhenLowKeywordPresent() {
        UpdatePriority priority = updatePrioritizer.prioritize("fix typo in readme", prioritization);

        assertThat(priority).isEqualTo(UpdatePriority.LOW);
    }

    @Test
    void shouldPreferHighOverLowWhenBothKeywordsPresent() {
        UpdatePriority priority = updatePrioritizer.prioritize("critical typo fix", prioritization);

        assertThat(priority).isEqualTo(UpdatePriority.HIGH);
    }
}
