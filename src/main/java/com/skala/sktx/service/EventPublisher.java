package com.skala.sktx.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Kafka 발행을 한 곳으로 모은 래퍼 클래스
// 컨트롤러/서비스가 직접 KafkaTemplate 쓰지 않고 이 클래스를 통해 발행하도록 함

@Service
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}
