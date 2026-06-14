package backend.academy.linktracker.scrapper.repository.jpa.chat;

import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.ResourceNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.link.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.model.ChatJpa;
import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaChatRepositoryWrapper implements ChatRepository {
    private final JpaChatRepository jpaChatRepository;
    private final JpaLinkRepository jpaLinkRepository;

    @Override
    @Transactional
    public void addChatLink(Long chatId, Long linkId) {
        ChatJpa chat =
                jpaChatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Чат не найден"));

        LinkJpa link =
                jpaLinkRepository.findById(linkId).orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена"));
        chat.getLinks().add(link);

        jpaChatRepository.save(chat);
    }

    @Override
    public Chat findById(Long chatId) {
        Optional<ChatJpa> chatJpa = jpaChatRepository.findById(chatId);
        return chatJpa.map(jpa -> new Chat(jpa.getChatId())).orElse(null);
    }

    @Override
    public void save(Chat chat) {
        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setChatId(chat.getChatId());
        jpaChatRepository.save(chatJpa);
    }
}
