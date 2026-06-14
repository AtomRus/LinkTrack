package backend.academy.linktracker.ai.filter;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class UpdateFilter {

    public boolean passes(RawLinkUpdate update, AiAgentProperties.Filtering filtering) {
        if (update.description().length() < filtering.getMinLength()) {
            return false;
        }
        if (isExcludedAuthor(update.author(), filtering)) {
            return false;
        }
        return !containsStopWord(update.description(), filtering);
    }

    private static boolean isExcludedAuthor(String author, AiAgentProperties.Filtering filtering) {
        String normalizedAuthor = author.toLowerCase(Locale.ROOT);
        return filtering.getExcludedAuthors().stream()
                .anyMatch(excluded -> excluded.toLowerCase(Locale.ROOT).equals(normalizedAuthor));
    }

    private static boolean containsStopWord(String description, AiAgentProperties.Filtering filtering) {
        String lowerDescription = description.toLowerCase(Locale.ROOT);
        return filtering.getStopWords().stream()
                .anyMatch(stopWord -> lowerDescription.contains(stopWord.toLowerCase(Locale.ROOT)));
    }
}
