package br.com.voting_system_vote_service.repository;

import br.com.voting_system_vote_service.entity.VoteSession;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author fsdney
 */

@Repository
public interface VoteSessionRepository extends JpaRepository<VoteSession, Long> {
	List<VoteSession> findByCreatorId(Long creatorId);
}
