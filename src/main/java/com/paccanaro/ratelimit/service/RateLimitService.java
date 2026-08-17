package com.paccanaro.ratelimit.service;

import com.paccanaro.ratelimit.dto.RateLimitRequest;
import com.paccanaro.ratelimit.dto.RateLimitResponse;
import com.paccanaro.ratelimit.exception.RedisIndisponivelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIXO_TOKENS = "rate_limit:%s:tokens";
    private static final String PREFIXO_RESET = "rate_limit:%s:reset";

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("RateLimitService iniciado com RedisTemplate={} / connectionFactory={}",
                redisTemplate.getClass().getName(),
                redisTemplate.getConnectionFactory() == null
                        ? "NULO"
                        : redisTemplate.getConnectionFactory().getClass().getName());
    }

    public RateLimitResponse verificarLimite(RateLimitRequest requisicao) {
        try {
            String chaveTokens = String.format(PREFIXO_TOKENS, requisicao.ip());
            String chaveReset = String.format(PREFIXO_RESET, requisicao.ip());

            log.debug("Verificando limite ip={} limite={} janela={}s (chaves: {} / {})",
                    requisicao.ip(), requisicao.limite(), requisicao.windowSeconds(),
                    chaveTokens, chaveReset);

            long tempoAtual = Instant.now().getEpochSecond();
            long tempoReset = obterTempoReset(chaveReset, tempoAtual, requisicao.windowSeconds());

            long tokensRestantes = obterTokensRestantes(chaveTokens, requisicao.limite());

            if (tokensRestantes > 0) {
                redisTemplate.opsForValue().decrement(chaveTokens);
                redisTemplate.expire(chaveTokens, requisicao.windowSeconds(), TimeUnit.SECONDS);

                return new RateLimitResponse(
                        true,
                        tokensRestantes - 1,
                        tempoReset,
                        requisicao.limite(),
                        requisicao.windowSeconds(),
                        null
                );
            } else {
                long tempoEspera = tempoReset - tempoAtual;
                return new RateLimitResponse(
                        false,
                        0,
                        tempoReset,
                        requisicao.limite(),
                        requisicao.windowSeconds(),
                        tempoEspera > 0 ? tempoEspera : 1
                );
            }
        } catch (DataAccessException e) {
            log.error("Falha de acesso ao Redis para ip={}", requisicao.ip(), e);
            throw new RedisIndisponivelException("Erro ao conectar com Redis", e);
        } catch (RuntimeException e) {
            // Não é falha de conexão: antes virava 503 "Redis desconectado" e escondia o bug real.
            log.error("Erro inesperado ao aplicar rate limit para ip={}", requisicao.ip(), e);
            throw e;
        }
    }

    private long obterTokensRestantes(String chaveTokens, int limite) {
        String valor = redisTemplate.opsForValue().get(chaveTokens);
        if (valor == null) {
            redisTemplate.opsForValue().set(chaveTokens, String.valueOf(limite));
            return limite;
        }
        return Long.parseLong(valor);
    }

    private long obterTempoReset(String chaveReset, long tempoAtual, int janelaSegundos) {
        String valor = redisTemplate.opsForValue().get(chaveReset);
        if (valor == null) {
            long novoReset = tempoAtual + janelaSegundos;
            redisTemplate.opsForValue().set(chaveReset, String.valueOf(novoReset));
            redisTemplate.expire(chaveReset, janelaSegundos, TimeUnit.SECONDS);
            return novoReset;
        }
        return Long.parseLong(valor);
    }


}
