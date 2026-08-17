package com.paccanaro.ratelimit.service;

import com.paccanaro.ratelimit.dto.RateLimitRequest;
import com.paccanaro.ratelimit.dto.RateLimitResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RateLimitService - Testes")
class RateLimitServiceTest {

    private RateLimitService servico;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        servico = new RateLimitService(redisTemplate);
    }

    @Test
    @DisplayName("Primeira requisição deve ser permitida")
    void testPrimeiraRequisicaoPermitida() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.decrement(anyString())).thenReturn(999L);

        RateLimitRequest requisicao = new RateLimitRequest("192.168.1.100", 1000, 60);


        RateLimitResponse resposta = servico.verificarLimite(requisicao);


        assertTrue(resposta.allowed());
        assertEquals(999, resposta.remaining());
    }

    @Test
    @DisplayName("Múltiplas requisições dentro do limite devem passar")
    void testMultiplasRequisicoesDentroDoLimite() {

        when(valueOperations.get(anyString()))
                .thenReturn("99")  // Primeira chamada retorna 99 tokens
                .thenReturn("99"); // Segunda chamada também
        when(valueOperations.decrement(anyString()))
                .thenReturn(98L)
                .thenReturn(97L);

        RateLimitRequest requisicao = new RateLimitRequest("192.168.1.100", 100, 60);

        RateLimitResponse resposta1 = servico.verificarLimite(requisicao);
        RateLimitResponse resposta2 = servico.verificarLimite(requisicao);

        assertTrue(resposta1.allowed());
        assertTrue(resposta2.allowed());
    }

    @Test
    @DisplayName("Requisição sem tokens deve ser bloqueada (429)")
    void testRequisicaoSemTokensBloqueada() {
        when(valueOperations.get(anyString())).thenReturn("0");

        RateLimitRequest requisicao = new RateLimitRequest("192.168.1.100", 5, 60);


        RateLimitResponse resposta = servico.verificarLimite(requisicao);


        assertFalse(resposta.allowed());
        assertEquals(0, resposta.remaining());
        assertNotNull(resposta.retryAfter());
    }

    @Test
    @DisplayName("IPs diferentes devem ter contadores independentes")
    void testIPsDiferentesIndependentes() {

        when(valueOperations.get(contains("192.168.1.100"))).thenReturn("99");
        when(valueOperations.get(contains("192.168.1.101"))).thenReturn("4");
        when(valueOperations.decrement(anyString()))
                .thenReturn(98L)
                .thenReturn(3L);

        RateLimitRequest requisicao1 = new RateLimitRequest("192.168.1.100", 100, 60);
        RateLimitRequest requisicao2 = new RateLimitRequest("192.168.1.101", 5, 60);


        RateLimitResponse resposta1 = servico.verificarLimite(requisicao1);
        RateLimitResponse resposta2 = servico.verificarLimite(requisicao2);


        assertTrue(resposta1.allowed());
        assertTrue(resposta2.allowed());
        assertEquals(98, resposta1.remaining());
        assertEquals(3, resposta2.remaining());
    }
}
