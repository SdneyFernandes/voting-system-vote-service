package br.com.voting_system_vote_service.dto;

import br.com.voting_system_vote_service.entity.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

import br.com.voting_system_vote_service.enums.VoteStatus;




/**
 * @author fsdney
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoteSessionDTO {

    @NotNull(message = "Data de início é obrigatória")
    @FutureOrPresent(message = "Data de início deve ser atual ou futura")
    private LocalDateTime startAt;

    @NotNull(message = "Data de término é obrigatória")
    @Future(message = "Data de término deve ser futura")
    private LocalDateTime endAt;

    @NotNull(message = "ID do criador é obrigatório")
    private Long creatorId;

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 100, message = "Título deve ter entre 5 e 100 caracteres")
    private String title;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "Opções são obrigatórias")
    @Size(min = 2, message = "Deve haver pelo menos 2 opções de voto")
    private List<@NotBlank String> options;

    // Somente na resposta
    private Long id;
    private VoteStatus status;

    public VoteSessionDTO() {
    }

    public VoteSessionDTO(VoteSession voteSession) {
        this.id = voteSession.getId();
        this.startAt = voteSession.getStartAt();
        this.endAt = voteSession.getEndAt();
        this.creatorId = voteSession.getCreatorId();
        this.title = voteSession.getTitle();
        this.description = voteSession.getDescription();
        this.options = voteSession.getOptions();
        this.status = voteSession.getStatus();
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        this.status = status;
    }
}
