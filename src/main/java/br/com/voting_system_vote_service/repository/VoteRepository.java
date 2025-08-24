package br.com.voting_system_vote_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.voting_system_vote_service.entity.*;

import java.util.List;


/**
 * @author fsdney
 */


@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    boolean existsByVoteSessionAndUserId(VoteSession voteSession, Long userId);

    List<Vote> findByVoteSession(VoteSession voteSession); 
    
    List<Vote> findByUserId(Long userId);
}
