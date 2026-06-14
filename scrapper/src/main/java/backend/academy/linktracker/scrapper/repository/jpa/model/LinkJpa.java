package backend.academy.linktracker.scrapper.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "link_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "link_url", nullable = false)
    private String linkUrl;

    @Column(name = "last_check_time")
    private OffsetDateTime lastCheckTime;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_etag")
    private String lastEtag;

    @ManyToMany
    @JoinTable(
            name = "link_tag_table",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<TagJpa> tags = new HashSet<>();

    @ManyToMany(mappedBy = "links")
    private Set<ChatJpa> chats = new HashSet<>();
}
