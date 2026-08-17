package com.paccanaro.ratelimit.config;

import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(DataRedisProperties propriedades) {
        RedisStandaloneConfiguration configuracao =
                new RedisStandaloneConfiguration(propriedades.getHost(), propriedades.getPort());

        if (propriedades.getUsername() != null) {
            configuracao.setUsername(propriedades.getUsername());
        }
        if (propriedades.getPassword() != null) {
            configuracao.setPassword(propriedades.getPassword());
        }
        configuracao.setDatabase(propriedades.getDatabase());

        return new LettuceConnectionFactory(configuracao);
    }

    @Bean
    @Primary
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
