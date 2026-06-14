package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.Link;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Этот сервис нужен для создания "чертежа" ссылки, ссылка будет сохранена тогда и только тогда,
 * когда она будет полгостью заполнена, а сам процесс заполнения можно в любой момент отменить
 */
@Service
@RequiredArgsConstructor
public class LinkSessionService {
    private final Map<Long, Link> userSessionStorage = new ConcurrentHashMap<>();

    public void updateSession(Link link) {
        userSessionStorage.put(link.getId(), link);
    }

    public Link getSession(Long chatId) {
        return userSessionStorage.get(chatId);
    }

    public void deleteDraft(Long chatId) {
        userSessionStorage.remove(chatId);
    }
}
