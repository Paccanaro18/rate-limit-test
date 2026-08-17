package com.paccanaro.ratelimit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RateLimitRequest(

        @NotBlank(message = "IP Não Pode Ser Vazio")
        String ip,
        @Positive(message = "O limite deve ser maior que 0")
        @Max(value = 1_000_000, message = "Limite não pode passar de 1,000,000")
        Integer limite,
        @Positive(message = "Os segundos da janela devem ser maiores que 0")
        @Max(value = 86_400, message = "A janela não pode exceder 86.400 segundos (1 dia)")
        Integer windowSeconds

) {
}
