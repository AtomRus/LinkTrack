package backend.academy.linktracker.scrapper.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, Long> {

    @Query(value = """
            select * from outbox_event e
            where e.status = 'NEW'
            order by e.created_at asc
            for update skip locked
            limit :batchSize
            """, nativeQuery = true)
    List<OutboxEventEntity> findNextNew(@Param("batchSize") int batchSize);
}
