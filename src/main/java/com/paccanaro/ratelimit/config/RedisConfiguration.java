package com.paccanaro.ratelimit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory fabricaConexao) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(fabricaConexao);

        StringRedisSerializer serializadorString = new StringRedisSerializer();

        template.setKeySerializer(serializadorString);
        template.setValueSerializer(serializadorString);
        template.setHashKeySerializer(serializadorString);
        template.setHashValueSerializer(serializadorString);

        template.afterPropertiesSet();
        return template;
    }
}
