package br.com.voting_system_vote_service.entity;

import br.com.voting_system_vote_service.enums.VoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * @author fsdney
 */

@Entity
@Table(name = "table_voteSession")
@NoArgsConstructor
@AllArgsConstructor
public class VoteSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255, nullable = false)
    private String description;

    @ElementCollection
    private List<String> options;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private Instant endAt;

    @Column(nullable = false)
    private Instant startAt;

    @Enumerated(EnumType.STRING)
    private VoteStatus status;

    // Atualiza status com base no horário atual em UTC
    public void updateStatus() {
        Instant now = Instant.now();
        if (now.isBefore(startAt)) {
            status = VoteStatus.NOT_STARTED;
        } else if (now.isAfter(endAt)) {
            status = VoteStatus.ENDED;
        } else {
            status = VoteStatus.ACTIVE;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        this.status = status;
    }
}
