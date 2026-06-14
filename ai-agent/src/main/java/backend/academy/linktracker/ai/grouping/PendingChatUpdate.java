package backend.academy.linktracker.ai.grouping;

import backend.academy.linktracker.ai.model.UpdatePriority;

record PendingChatUpdate(long id, String description, UpdatePriority priority) {}
