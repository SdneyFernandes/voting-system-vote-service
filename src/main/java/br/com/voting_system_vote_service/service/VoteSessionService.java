package br.com.voting_system_vote_service.service;


import br.com.voting_system_vote_service.dto.*;
import br.com.voting_system_vote_service.entity.*;
import br.com.voting_system_vote_service.enums.VoteStatus;
import br.com.voting_system_vote_service.repository.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author fsdney
 */

@Service
@RequiredArgsConstructor
public class VoteSessionService {

    private final Logger logger = LoggerFactory.getLogger(VoteSessionService.class);

    private final MeterRegistry meterRegistry;
    private final VoteSessionRepository voteSessionRepository;
    private final VoteRepository voteRepository;
    private final RestTemplate restTemplate;
    
    private static final String USER_SERVICE_URL = "http://voting-system-user-service/api/users/";

     public VoteSessionDTO createVoteSession(VoteSessionDTO voteSessionDTO) {
        logger.info("[CREATE] Criando nova sessão de votação...");
        meterRegistry.counter("votacao.criar.chamadas").increment();

        if (voteSessionDTO == null || voteSessionDTO.getCreatorId() == null) {
            throw new IllegalArgumentException("DTO da sessão ou ID do criador não pode ser nulo");
        }

        UserDTO creator;
        try {
            // 1. Pegar os cabeçalhos X-User-Id e X-User-Role da requisição original
            String userId = getHeaderFromCurrentRequest("X-User-Id");
            String userRole = getHeaderFromCurrentRequest("X-User-Role");

            // 2. Criar os cabeçalhos para a nova requisição
            HttpHeaders headers = new HttpHeaders();
            if (userId != null && userRole != null) {
                headers.set("X-User-Id", userId);
                headers.set("X-User-Role", userRole);
                logger.info("Propagando cabeçalhos - X-User-Id: {}, X-User-Role: {}", userId, userRole);
            } else {
                logger.warn("Cabeçalhos de autenticação não encontrados na requisição original.");
            }
            
            // 3. Criar a entidade da requisição com os cabeçalhos
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 4. Usar restTemplate.exchange() para enviar a requisição com os cabeçalhos corretos
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    USER_SERVICE_URL + voteSessionDTO.getCreatorId(),
                    HttpMethod.GET,
                    entity,
                    UserDTO.class);
                    
            creator = response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("[USER-SERVICE] Usuário com ID {} não encontrado.", voteSessionDTO.getCreatorId());
            throw new IllegalStateException("Usuário criador não encontrado");
        } catch (RestClientException e) {
            logger.error("[USER-SERVICE] Erro ao acessar o serviço de usuários", e);
            throw new IllegalStateException("Falha na comunicação com o serviço de usuários", e);
        }
        
        // O resto do seu método continua exatamente igual
        if (creator == null || creator.getId() == null) {
            throw new IllegalStateException("Usuário criador inválido ou resposta inválida do serviço de usuários");
        }

        if (!"ADMIN".equalsIgnoreCase(creator.getRole())) {
            logger.warn("[PERMISSÃO] Usuário {} sem permissão (Role: {})", creator.getId(), creator.getRole());
            throw new SecurityException("Apenas administradores podem criar sessões de votação");
        }

        // ... etc
        
        // (cole o resto do seu método aqui)
        
        if (voteSessionDTO.getStartAt() == null || voteSessionDTO.getEndAt() == null) {
            throw new IllegalArgumentException("Datas de início e término são obrigatórias");
        }

        if (voteSessionDTO.getStartAt().isAfter(voteSessionDTO.getEndAt())) {
            logger.error("[VALIDAÇÃO] Datas inválidas: início ({}) depois do término ({})",
                    voteSessionDTO.getStartAt(), voteSessionDTO.getEndAt());
            throw new IllegalArgumentException("A data de início deve ser antes da data de término");
        }

        if (voteSessionDTO.getEndAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é permitido criar sessão com data já expirada");
        }

        if (voteSessionDTO.getOptions() == null || voteSessionDTO.getOptions().size() < 2) {
            logger.error("[VALIDAÇÃO] Número insuficiente de opções: {}",
                    voteSessionDTO.getOptions() == null ? "null" : voteSessionDTO.getOptions().size());
            throw new IllegalArgumentException("É necessário fornecer pelo menos 2 opções de voto");
        }

        VoteStatus status;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voteSessionDTO.getStartAt())) {
            status = VoteStatus.NOT_STARTED;
        } else {
            status = VoteStatus.ACTIVE;
        }

        VoteSession voteSession = new VoteSession();
        voteSession.setTitle(voteSessionDTO.getTitle());
        voteSession.setDescription(voteSessionDTO.getDescription());
        voteSession.setOptions(voteSessionDTO.getOptions());
        voteSession.setStartAt(voteSessionDTO.getStartAt());
        voteSession.setEndAt(voteSessionDTO.getEndAt());
        voteSession.setCreatorId(creator.getId());
        voteSession.setStatus(status);

        VoteSession saved = voteSessionRepository.save(voteSession);
        logger.info("[CREATE] Sessão criada com ID: {}", saved.getId());

        return new VoteSessionDTO(saved);
    }

    // Adicione este novo método auxiliar na sua classe
    private String getHeaderFromCurrentRequest(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(headerName);
        }
        return null;
    }
    
    
    public List<VoteSessionDTO> getAllVoteSessions() {
        logger.info("[LISTAR] Listando todas as sessões de votação...");
        meterRegistry.counter("listar.votacoes.chamadas").increment();

        return voteSessionRepository.findAll().stream()
                .peek(VoteSession::updateStatus)
                .map(VoteSessionDTO::new)
                .collect(Collectors.toList());
    }

    public VoteSessionDTO getVoteSession(Long id) {
        logger.info("[GET] Buscando sessão ID {}", id);
        VoteSession session = voteSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sessão de votação não encontrada"));

        session.updateStatus();
        return new VoteSessionDTO(session);
    }

    public void deleteVoteSession(Long id) {
        logger.info("[DELETE] Removendo sessão ID {}", id);
        VoteSession session = voteSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada"));

        if (!voteRepository.findByVoteSession(session).isEmpty()) {
            logger.warn("[DELETE] Tentativa de excluir sessão com votos registrados");
            throw new RuntimeException("Não é possível excluir uma sessão com votos registrados");
        }

        voteSessionRepository.delete(session);
        logger.info("[DELETE] Sessão excluída com sucesso");
    }

    public Map<String, Object> getVoteResults(Long id) {
        logger.info("[RESULTADO] Resultado da sessão ID {}", id);
        VoteSession session = voteSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada"));

        Map<String, Long> resultado = voteRepository.findByVoteSession(session).stream()
                .collect(Collectors.groupingBy(Vote::getChosenOption, Collectors.counting()));

        if (resultado.isEmpty()) {
            logger.info("[RESULTADO] Nenhum voto registrado para a sessão ID {}", id);
            return Map.of("mensagem", "Nenhum voto registrado ainda para esta sessão.");
        }

        long total = resultado.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalVotos", total);
        response.put("resultado", resultado);

        logger.info("[RESULTADO] Resultado computado: {} (total: {})", resultado, total);
        return response;
    }
    
    public List<VoteSessionDTO> getSessionsCreatedByUser(Long userId) {
        logger.info("[BUSCA] Sessões criadas pelo usuário ID {}", userId);
        meterRegistry.counter("sessoes.criadas.usuario.chamadas").increment();
        long start = System.currentTimeMillis();

        List<VoteSessionDTO> sessions = voteSessionRepository.findByCreatorId(userId).stream()
                .peek(VoteSession::updateStatus)
                .map(VoteSessionDTO::new)
                .toList();

        long duration = System.currentTimeMillis() - start;
        meterRegistry.timer("sessoes.criadas.usuario.tempo").record(duration, TimeUnit.MILLISECONDS);
        logger.info("[BUSCA] {} sessões criadas encontradas para usuário {}", sessions.size(), userId);

        return sessions;
    }


    
    public List<VoteSessionDTO> getSessionsByStatus(VoteStatus status) {
        logger.info("[BUSCA] Sessões com status {}", status);
        meterRegistry.counter("sessoes.status.chamadas").increment();
        long start = System.currentTimeMillis();

        List<VoteSessionDTO> sessions = voteSessionRepository.findAll().stream()
                .peek(VoteSession::updateStatus)
                .filter(session -> session.getStatus() == status)
                .map(VoteSessionDTO::new)
                .toList();

        long duration = System.currentTimeMillis() - start;
        meterRegistry.timer("sessoes.status.tempo").record(duration, TimeUnit.MILLISECONDS);
        logger.info("[BUSCA] {} sessões encontradas com status {}", sessions.size(), status);

        return sessions;
    }

    
   public List<VoteSessionDTO> getSessionsVotedByUser(Long userId) {
        logger.info("[BUSCA] Sessões votadas pelo usuário ID {}", userId);
        try {
            List<VoteSessionDTO> sessions = voteRepository.findByUserId(userId).stream()
                    .map(Vote::getVoteSession)
                    .distinct()
                    .peek(VoteSession::updateStatus)
                    .map(VoteSessionDTO::new)
                    .toList();
            logger.info("[BUSCA] {} sessões encontradas", sessions.size());
            return sessions;
        } catch (Exception e) {
            logger.error("[ERRO] Falha ao buscar sessões votadas", e);
            throw new RuntimeException("Falha ao buscar sessões votadas", e);
        }
    }
}