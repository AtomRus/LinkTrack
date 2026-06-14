package backend.academy.linktracker.ai.dto;

import backend.academy.linktracker.ai.model.UpdatePriority;
import java.util.List;

public record ProcessedLinkUpdate(Long id, String description, List<Long> tgChatIds, UpdatePriority priority) {}
