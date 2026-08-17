package com.paccanaro.ratelimit.exception;

public class RedisIndisponivelException extends RuntimeException {
    public RedisIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public RedisIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

}
