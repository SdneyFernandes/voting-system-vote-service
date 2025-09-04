package br.com.voting_system_vote_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.voting_system_vote_service.service.VoteSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.voting_system_vote_service.dto. *;
import br.com.voting_system_vote_service.enums.VoteStatus;


import java.util. *;




/**
 * @author fsdney
 */

@RestController
@RequestMapping("/api/votes_session")
@Tag(name = "Vote Session", description = "Gerenciamento de sessões de votação")
public class VoteSessionController {
	
	private static final Logger logger = LoggerFactory.getLogger(VoteSessionController.class);
	
	
	@Autowired
	private VoteSessionService voteSessionService;
	
	@Operation(summary = "Criar nova sessão de votação")
	@PostMapping("/create")
	public ResponseEntity<VoteSessionDTO> createVoteSession(@RequestBody VoteSessionDTO voteSessionDTO) {
	    logger.info("Recebida requisição para criar nova Sessão de Votação");
	    VoteSessionDTO result = voteSessionService.createVoteSession(voteSessionDTO);
	    return ResponseEntity.ok(result);
	}
	
	
	@Operation(summary = "Listar todas as sessões de votação")
	@GetMapping
    public ResponseEntity<List<VoteSessionDTO>> getAllVoteSessions() {
		logger.info("Recebida requisição para listar todas as Sessões de Votação");
        List<VoteSessionDTO> sessions = voteSessionService.getAllVoteSessions();
        return ResponseEntity.ok(sessions);
    }
	
	@Operation(summary = "Buscar sessão de votação por ID")
    @GetMapping("/{voteSessionId}")
    public ResponseEntity<VoteSessionDTO> getVoteSession(@PathVariable Long voteSessionId) {
    	logger.info("Recebida requisição para buscar Sessão de Votação");
        VoteSessionDTO session = voteSessionService.getVoteSession(voteSessionId);
        return ResponseEntity.ok(session);
    }

	@Operation(summary = "Excluir sessão de votação")
    @DeleteMapping("/{voteSessionId}")
    public ResponseEntity<String> deleteVoteSession(@PathVariable Long voteSessionId) {
    	logger.info("Recebida requisição para deletar Sessão de Votação");
        voteSessionService.deleteVoteSession(voteSessionId);
        return ResponseEntity.ok("Sessão de votação deletada com sucesso");
    }

	@Operation(summary = "Obter resultados da sessão de votação")
    @GetMapping("/{voteSessionId}/results")
	public ResponseEntity<Map<String, Object>> getVoteResults(@PathVariable Long voteSessionId) {
		logger.info("Recebida requisição para buscar resultados da sessão de votação");
		Map<String, Object> results = voteSessionService.getVoteResults(voteSessionId);
	    return ResponseEntity.ok(results);
	}
	
	@Operation(summary = "Sessões criadas por um usuário (admin)")
	@GetMapping("/created")
    public ResponseEntity<List<VoteSessionDTO>> getSessionsCreatedByUser(@RequestParam Long userId) {
		logger.info("Recebida requisição para buscar Sessões criadas por um usuário (admin)");
		List<VoteSessionDTO> sessions = voteSessionService.getSessionsCreatedByUser(userId);
        return ResponseEntity.ok(sessions);
    }
	
	@Operation(summary = "Sessões filtradas por status")
    @GetMapping("/status")
    public ResponseEntity<List<VoteSessionDTO>> getSessionsByStatus(@RequestParam VoteStatus status) {
		logger.info("Recebida requisição para filtradas sessões por status");
		List<VoteSessionDTO> sessions = voteSessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }
	
	@Operation(summary = "Buscar voto por usuario", description = "Sessões onde o usuário votou (qualquer perfil)")
	@GetMapping("/voted")
	public ResponseEntity<?> getSessionsVotedByUser(@RequestParam Long userId) {
	    try {
	        List<VoteSessionDTO> sessions = voteSessionService.getSessionsVotedByUser(userId);
	        return ResponseEntity.ok(sessions);
	    } catch (Exception e) {
	        logger.error("Erro ao buscar sessões votadas", e);
	        return ResponseEntity.status(500).body("Erro ao processar requisição");
	    }
	}
	
}
