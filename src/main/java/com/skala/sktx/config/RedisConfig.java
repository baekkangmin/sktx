package com.skala.sktx.config;

import com.skala.sktx.service.HoldExpiryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

// Redis 연결용 StringRedisTemplate Bean 생성

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Redis Keyspace Notification 이벤트를 수신함.
     * docker-compose에서 notify-keyspace-events Ex 로 켜져 있어야 함.
     *
     * 만료 이벤트 채널(기본 DB 0):
     *   __keyevent@0__:expired
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory cf,
            MessageListenerAdapter expiredListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);
        container.addMessageListener(expiredListenerAdapter, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }

    @Bean
    public MessageListenerAdapter expiredListenerAdapter(HoldExpiryService holdExpiryService) {
        // onMessage(String expiredKey) 메서드를 호출
        return new MessageListenerAdapter(holdExpiryService, "onMessage");
    }
}