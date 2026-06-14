package backend.academy.linktracker.ai.prioritization;

import backend.academy.linktracker.ai.model.UpdatePriority;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class UpdatePrioritizer {

    public UpdatePriority prioritize(String description, AiAgentProperties.Prioritization prioritization) {
        String lowerDescription = description.toLowerCase(Locale.ROOT);
        if (containsKeyword(lowerDescription, prioritization.getHighKeywords())) {
            return UpdatePriority.HIGH;
        }
        if (containsKeyword(lowerDescription, prioritization.getLowKeywords())) {
            return UpdatePriority.LOW;
        }
        return UpdatePriority.MEDIUM;
    }

    private static boolean containsKeyword(String lowerDescription, java.util.List<String> keywords) {
        return keywords.stream().anyMatch(keyword -> lowerDescription.contains(keyword.toLowerCase(Locale.ROOT)));
    }
}
