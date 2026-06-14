package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "sql")
public class SqlLinkRepositoryImpl implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SqlChatRepositoryImpl sqlChatDAO;

    @Override
    @Transactional
    public Link addLink(Link link) {

        String sql = """
            INSERT INTO link_table (link_url, last_check_time, updated_at)
            VALUES (?, ?, ?)
            ON CONFLICT (link_url) DO UPDATE SET link_url = EXCLUDED.link_url
            RETURNING link_id
            """;

        Long generatedId = jdbcTemplate.queryForObject(
                sql, Long.class, link.getLinkUrl(), link.getLastCheckTime(), link.getUpdatedAt());

        link.setLinkId(generatedId);

        sqlChatDAO.addChatLink(link.getChatId(), generatedId);

        return link;
    }

    @Override
    public List<Link> getListOfLinksByChatId(Long chatId, int limit, int offset) {
        String sql = """
            SELECT l.link_id, l.link_url, l.last_etag
            FROM link_table l
            JOIN chat_link_table cl ON l.link_id = cl.link_id
            WHERE cl.chat_id = ?
            ORDER BY l.link_id
            LIMIT ? OFFSET ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Link(
                        chatId,
                        rs.getLong("link_id"),
                        rs.getString("link_url"),
                        null,
                        null,
                        rs.getString("last_etag"),
                        new ArrayList<>()),
                chatId,
                limit,
                offset);
    }

    @Override
    public List<Link> getListOfLinksByChatId(Long chatId) {
        String sql = """
            SELECT l.link_id, l.link_url
            FROM link_table l
            JOIN chat_link_table cl ON l.link_id = cl.link_id
            WHERE cl.chat_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Link(
                        chatId,
                        rs.getLong("link_id"),
                        rs.getString("link_url"),
                        null,
                        null,
                        rs.getString("last_etag"),
                        new ArrayList<>()),
                chatId);
    }

    @Override
    public List<Link> getListOfLinksByChatIdAndTag(Long chatId, String tag) {
        String sql = """
            SELECT l.link_id, l.link_url
            FROM link_table l
            JOIN chat_link_table cl ON l.link_id = cl.link_id
            JOIN link_tag_table lt ON l.link_id = lt.link_id
            JOIN tag_table t ON lt.tag_id = t.tag_id
            WHERE cl.chat_id = ? AND t.tag = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Link(
                        chatId,
                        rs.getLong("link_id"),
                        rs.getString("link_url"),
                        null,
                        null,
                        rs.getString("last_etag"),
                        new ArrayList<>()),
                chatId,
                tag);
    }

    @Override
    public void removeLinkByURL(Long chatId, String url) {
        Long linkId = jdbcTemplate
                .query("SELECT link_id FROM link_table WHERE link_url = ?", (rs, rowNum) -> rs.getLong("link_id"), url)
                .stream()
                .findFirst()
                .orElse(null);
        if (linkId == null) {
            return;
        }

        String deleteChatLinkSql = """
            DELETE FROM chat_link_table
            WHERE chat_id = ? AND link_id = ?
            """;
        jdbcTemplate.update(deleteChatLinkSql, chatId, linkId);

        Integer bindingsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_link_table WHERE link_id = ?", Integer.class, linkId);
        if (bindingsCount != null && bindingsCount == 0) {
            jdbcTemplate.update("DELETE FROM link_table WHERE link_id = ?", linkId);
        }
    }

    @Override
    @Transactional
    public void addTagToLink(long linkId, long tagId) {
        String sql = "INSERT INTO link_tag_table (link_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, linkId, tagId);
    }

    @Override
    public List<Long> findChatIdsByUrl(String url) {
        String sql = """
        SELECT cl.chat_id
        FROM chat_link_table cl
        JOIN link_table l ON cl.link_id = l.link_id
        WHERE l.link_url = ?
        """;
        return jdbcTemplate.queryForList(sql, Long.class, url);
    }

    @Override
    public List<Link> findAll() {
        String sql = """
        SELECT cl.chat_id, l.link_id, l.link_url
        FROM link_table l
        JOIN chat_link_table cl ON l.link_id = cl.link_id
        """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Link(
                        rs.getLong("chat_id"),
                        rs.getLong("link_id"),
                        rs.getString("link_url"),
                        null,
                        null,
                        rs.getString("last_etag"),
                        new ArrayList<>()));
    }

    @Override
    public Optional<Link> findLinkByUrl(String url) {
        String sql = """
        SELECT cl.chat_id, l.link_id, l.link_url
        FROM link_table l
        JOIN chat_link_table cl ON l.link_id = cl.link_id
        WHERE l.link_url = ?
        """;

        List<Link> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Link(
                        rs.getLong("chat_id"),
                        rs.getLong("link_id"),
                        rs.getString("link_url"),
                        null,
                        null,
                        rs.getString("last_etag"),
                        new ArrayList<>()),
                url);

        return results.stream().findFirst();
    }

    @Override
    public List<Link> findAssignedLinksToUser() {
        String sql = """
        SELECT cl.chat_id, l.link_id, l.link_url, l.last_check_time, l.updated_at
        FROM link_table l
        JOIN chat_link_table cl ON l.link_id = cl.link_id
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Link link = new Link();
            link.setChatId(rs.getLong("chat_id"));
            link.setLinkId(rs.getLong("link_id"));
            link.setLinkUrl(rs.getString("link_url"));

            Timestamp lastCheckTimestamp = rs.getTimestamp("last_check_time");
            if (lastCheckTimestamp != null) {
                link.setLastCheckTime(lastCheckTimestamp.toInstant().atOffset(ZoneOffset.UTC));
            }

            Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
            if (updatedAtTimestamp != null) {
                link.setUpdatedAt(updatedAtTimestamp.toInstant().atOffset(ZoneOffset.UTC));
            }
            return link;
        });
    }

    @Override
    public void updateLink(Link link) {
        String sql = """
        UPDATE link_table
        SET link_url = ?, last_check_time = ?, updated_at = ?, last_etag = ?
        WHERE link_table.link_id = ?
        """;

        jdbcTemplate.update(
                sql,
                link.getLinkUrl(),
                link.getLastCheckTime(),
                link.getUpdatedAt(),
                link.getLastEtag(),
                link.getLinkId());
    }

    @Override
    public void removeTagFromLink(long linkId, long tagId) {
        jdbcTemplate.update("DELETE FROM link_tag_table WHERE link_id = ? AND tag_id = ?", linkId, tagId);
    }
}
