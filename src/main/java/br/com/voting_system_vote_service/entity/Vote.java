package br.com.voting_system_vote_service.entity;

import jakarta.persistence. *;
import lombok. *;

import java.time.LocalDateTime;

/**
 * @author fsdney
 */

@Entity
@Table(name = "table_vote")
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long userId;
	
	@ManyToOne
	@JoinColumn(name = "voteSession_id", nullable = false)
	private VoteSession voteSession;
	
	@Column(nullable = false)
	private String chosenOption;
	
	@Column(nullable = false) 
	private LocalDateTime createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public VoteSession getVoteSession() {
		return voteSession;
	}

	public void setVoteSession(VoteSession voteSession) {
		this.voteSession = voteSession;
	}

	public String getChosenOption() {
		return chosenOption;
	}

	public void setChosenOption(String chosenOption) {
		this.chosenOption = chosenOption;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	
}
