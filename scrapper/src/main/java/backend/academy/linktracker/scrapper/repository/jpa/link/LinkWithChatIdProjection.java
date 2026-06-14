package backend.academy.linktracker.scrapper.repository.jpa.link;

import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;

public interface LinkWithChatIdProjection {
    LinkJpa getEntity();

    Long getChatId();
}
