package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "sql")
public class SqlTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long addTag(String tag) {
        String sql = "INSERT INTO tag_table (tag) VALUES (?) " + "ON CONFLICT (tag) DO UPDATE SET tag = EXCLUDED.tag "
                + "RETURNING tag_id";
        return jdbcTemplate.queryForObject(sql, Long.class, tag);
    }

    @Override
    public void removeTag(long tagId) {
        int affectedRows = jdbcTemplate.update("DELETE FROM tag_table WHERE tag_id = ?", tagId);
        if (affectedRows == 0) {
            throw new TagNotFoundException("Тег с ID " + tagId + " не найден");
        }
    }

    @Override
    public void updateTag(long tagId, String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Название тега не может быть пустым");
        }
        int affectedRows = jdbcTemplate.update("UPDATE tag_table SET tag = ? WHERE tag_id = ?", tag, tagId);
        if (affectedRows == 0) {
            throw new TagNotFoundException("Тег с ID " + tagId + " не найден");
        }
    }

    @Override
    public Tag getTag(long tagId) {
        String sql = "SELECT tag_id, tag FROM tag_table WHERE tag_id = ?";
        return jdbcTemplate
                .query(sql, (rs, rowNum) -> new Tag(rs.getLong("tag_id"), rs.getString("tag")), tagId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT tag_id, tag FROM tag_table WHERE tag = ?";
        try {
            Tag tag = jdbcTemplate.queryForObject(
                    sql, (rs, rowNum) -> new Tag(rs.getLong("tag_id"), rs.getString("tag")), name);
            return Optional.ofNullable(tag);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
