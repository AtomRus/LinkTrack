package backend.academy.linktracker.scrapper.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "chat_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatJpa {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @ManyToMany
    @JoinTable(
            name = "chat_link_table",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id"))
    private Set<LinkJpa> links = new HashSet<>();
}
