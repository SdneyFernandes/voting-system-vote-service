package br.com.voting_system_vote_service.controller;

import br.com.voting_system_vote_service.service. *;
import br.com.voting_system_vote_service.dto.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;



/**
 * @author fsdney
 */

@RestController
@RequestMapping("/api/votes")
@Tag(name = "Vote", description = "Endpoints relacionados ao registro de votos")
public class VoteController {

	private static final Logger logger = LoggerFactory.getLogger(VoteController.class);
	
	@Autowired
	private VoteService voteService;
	
	
	@Operation(summary = "Registrar um voto", description = "Registra um voto para uma sessão existente.")
	@PostMapping("/{voteSessionId}/cast")
    public ResponseEntity<String> castVote(
            @PathVariable Long voteSessionId,
            @RequestParam(required = true) Long userId,
            @RequestParam(required = true) String option) {
		logger.info("Recebida requisição para registar voto");
        String responseMessage = voteService.castVote(voteSessionId, userId, option);
        return ResponseEntity.ok(responseMessage);
    }
	
}












