package com.skala.sktx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

// Kafka 관련 설정 및 토픽 생성(NewTopic)을 정의

@Configuration
public class KafkaConfig {

    public static final String TOPIC_WAIT_ENQUEUE = "wait.enqueue";
    public static final String TOPIC_WAIT_ADMIT = "wait.admit";
    public static final String TOPIC_SEAT_HOLD_REQUESTED = "seat.hold.requested";
    public static final String TOPIC_SEAT_HELD = "seat.held";
    public static final String TOPIC_SEAT_HOLD_FAILED = "seat.hold.failed";
    public static final String TOPIC_PAYMENT_CONFIRMED = "payment.confirmed";

    @Bean
    public NewTopic waitEnqueueTopic() { return TopicBuilder.name(TOPIC_WAIT_ENQUEUE).partitions(3).replicas(1).build(); }

    @Bean
    public NewTopic waitAdmitTopic() { return TopicBuilder.name(TOPIC_WAIT_ADMIT).partitions(3).replicas(1).build(); }

    @Bean
    public NewTopic seatHoldRequestedTopic() { return TopicBuilder.name(TOPIC_SEAT_HOLD_REQUESTED).partitions(3).replicas(1).build(); }

    @Bean
    public NewTopic seatHeldTopic() { return TopicBuilder.name(TOPIC_SEAT_HELD).partitions(3).replicas(1).build(); }

    @Bean
    public NewTopic seatHoldFailedTopic() { return TopicBuilder.name(TOPIC_SEAT_HOLD_FAILED).partitions(3).replicas(1).build(); }

    @Bean
    public NewTopic paymentConfirmedTopic() { return TopicBuilder.name(TOPIC_PAYMENT_CONFIRMED).partitions(3).replicas(1).build(); }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(props);
        pf.setValueSerializer(new JsonSerializer<>(objectMapper));
        return pf;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
