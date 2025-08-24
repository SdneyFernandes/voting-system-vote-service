package br.com.voting_system_vote_service.repository;

import br.com.voting_system_vote_service.entity.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {
    void deleteAllBySessionId(Long sessionId);
}
