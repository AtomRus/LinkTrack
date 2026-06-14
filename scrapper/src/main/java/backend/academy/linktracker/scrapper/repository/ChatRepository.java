package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Chat;

public interface ChatRepository {
    void addChatLink(Long chatId, Long linkId);

    Chat findById(Long chatId);

    void save(Chat chat);
}
