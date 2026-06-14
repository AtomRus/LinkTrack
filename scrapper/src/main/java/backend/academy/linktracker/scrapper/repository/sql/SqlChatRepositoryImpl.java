package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "sql")
public class SqlChatRepositoryImpl implements ChatRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addChatLink(Long chatId, Long linkId) {
        jdbcTemplate.update("INSERT INTO chat_table (chat_id) VALUES (?) ON CONFLICT DO NOTHING", chatId);

        String sql = "INSERT INTO chat_link_table (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, chatId, linkId);
    }

    @Override
    public Chat findById(Long chatId) {
        List<Chat> chat = jdbcTemplate.query(
                "SELECT chat_table.chat_id FROM chat_table WHERE chat_table.chat_id = ?",
                (rs, rowNum) -> new Chat(rs.getLong("chat_id")),
                chatId);
        if (chat.isEmpty()) {
            return null;
        }
        return chat.getFirst();
    }

    @Override
    public void save(Chat chat) {
        jdbcTemplate.update("INSERT INTO chat_table (chat_id) VALUES (?) ON CONFLICT DO NOTHING", chat.getChatId());
    }
}
