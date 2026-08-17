package com.paccanaro.ratelimit.controller;

import com.paccanaro.ratelimit.dto.RateLimitRequest;
import com.paccanaro.ratelimit.dto.RateLimitResponse;
import com.paccanaro.ratelimit.exception.RedisIndisponivelException;
import com.paccanaro.ratelimit.exception.ValidacaoException;
import com.paccanaro.ratelimit.service.RateLimitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RateLimitController {
    private final RateLimitService servicoRateLimit;

    public RateLimitController(RateLimitService servicoRateLimit) {
        this.servicoRateLimit = servicoRateLimit;
    }

    @PostMapping("/check-limit")
    public ResponseEntity<RateLimitResponse> verificarLimite(
            @Valid @RequestBody RateLimitRequest requisicao) {

        validarIp(requisicao.ip());

        RateLimitResponse resposta = servicoRateLimit.verificarLimite(requisicao);

        if (!resposta.allowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resposta);
        }

        return ResponseEntity.ok(resposta);
    }

    private void validarIp(String ip) {
        if (!ip.matches("^((25[0-5]|(2[0-4]|1\\d)?\\d)\\.?\\b){4}$|^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4})$")) {
            throw new ValidacaoException("IP inválido: " + ip);
        }
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(ValidacaoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(400, "Validação falhou", e.getMessage()));
    }

    @ExceptionHandler(RedisIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarRedisIndisponivel(RedisIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse(503, "Serviço indisponível", "Redis desconectado"));
    }

    record ErroResponse(int status, String erro, String mensagem) {}
}
