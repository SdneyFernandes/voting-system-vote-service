package br.com.voting_system_vote_service.service;

import br.com.voting_system_vote_service.dto.UserDTO;
import br.com.voting_system_vote_service.entity.Vote;
import br.com.voting_system_vote_service.entity.VoteResult;
import br.com.voting_system_vote_service.entity.VoteSession;
import br.com.voting_system_vote_service.repository.VoteRepository;
import br.com.voting_system_vote_service.repository.VoteResultRepository;
import br.com.voting_system_vote_service.repository.VoteSessionRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author fsdney
 */

@Service
@RequiredArgsConstructor
public class VoteService {

    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);

    private final RestTemplate restTemplate;
    private final VoteRepository voteRepository;
    private final VoteSessionRepository voteSessionRepository;
    private final VoteSessionService voteSessionService;
    private final VoteResultRepository voteResultRepository;
    private final MeterRegistry meterRegistry;

    private static final String USER_SERVICE_URL = "http://voting-system-user-service/api/users/";

    public String castVote(Long voteSessionId, Long userId, String option, String xUserIdHeader, String xUserRoleHeader) {
        long start = System.currentTimeMillis();
        meterRegistry.counter("votos.chamadas").increment();

        logger.info("Usuário {} tentando votar na sessão {}", userId, voteSessionId);

        UserDTO user;
        try {
            HttpHeaders headers = new HttpHeaders();
            if (xUserIdHeader != null && xUserRoleHeader != null) {
                headers.set("X-User-Id", xUserIdHeader);
                headers.set("X-User-Role", xUserRoleHeader);
                logger.info("Headers propagados para User Service: X-User-Id={}, X-User-Role={}", xUserIdHeader, xUserRoleHeader);
            } else {
                logger.warn("Headers de autenticação não recebidos");
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    USER_SERVICE_URL + userId,
                    HttpMethod.GET,
                    entity,
                    UserDTO.class
            );
            user = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Usuário {} não encontrado", userId);
            throw new IllegalStateException("Usuário não encontrado. O voto não pode ser registrado.");
        } catch (RestClientException e) {
            logger.error("Erro ao comunicar com USER-SERVICE", e);
            throw new IllegalStateException("Falha ao buscar usuário. O voto não pode ser registrado.", e);
        }

        if (user == null || user.getId() == null) {
            throw new IllegalStateException("Usuário inválido ou resposta inválida do serviço de usuários");
        }

        VoteSession session = voteSessionRepository.findById(voteSessionId)
        .orElseThrow(() -> new RuntimeException("Sessão de votação não encontrada"));

        Instant now = Instant.now();
        if (now.isBefore(session.getStartAt())) {
            logger.warn("Sessão {} ainda não iniciou", voteSessionId);
            throw new RuntimeException("A votação ainda não está aberta");
        }
        if (now.isAfter(session.getEndAt())) {
            logger.warn("Sessão {} encerrada", voteSessionId);
            throw new RuntimeException("A votação já foi encerrada");
        }

        if (!session.getOptions().contains(option)) {
            logger.warn("Opção inválida: {}", option);
            throw new RuntimeException("Opção de voto inválida");
        }

        if (voteRepository.existsByVoteSessionAndUserId(session, userId)) {
            logger.warn("Usuário {} já votou na sessão {}", userId, voteSessionId);
            throw new RuntimeException("Usuário já votou nesta votação");
        }

        Vote vote = new Vote();
        vote.setVoteSession(session);
        vote.setUserId(userId);
        vote.setChosenOption(option);
        voteRepository.save(vote);

        try {
            Map<String, Object> results = voteSessionService.getVoteResults(voteSessionId);
            voteResultRepository.deleteAllBySessionId(voteSessionId);

            @SuppressWarnings("unchecked")
            Map<String, Long> resultadoMap = (Map<String, Long>) results.get("resultado");
            if (resultadoMap != null) {
                for (Map.Entry<String, Long> entry : resultadoMap.entrySet()) {
                    VoteResult result = new VoteResult();
                    result.setSessionId(voteSessionId);
                    result.setOption(entry.getKey());
                    result.setTotalVotes(entry.getValue());
                    voteResultRepository.save(result);
                }
            }

            logger.info("Resultados da sessão {} atualizados", voteSessionId);
        } catch (Exception e) {
            logger.error("Erro ao processar resultados", e);
        }

        meterRegistry.timer("votos.chamadas.tempo")
                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);

        logger.info("Voto registrado: usuário {} sessão {}", userId, voteSessionId);
        return "Voto registrado com sucesso!";
    }
}