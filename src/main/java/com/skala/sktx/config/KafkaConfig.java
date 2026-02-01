package com.skala.sktx.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_WAIT_ENQUEUE = "wait.enqueue";
    public static final String TOPIC_WAIT_ADMIT = "wait.admit";
    public static final String TOPIC_SEAT_HOLD_REQUESTED = "seat.hold.requested";
    public static final String TOPIC_SEAT_HELD = "seat.held";
    public static final String TOPIC_SEAT_HOLD_FAILED = "seat.hold.failed";
    public static final String TOPIC_PAYMENT_CONFIRMED = "payment.confirmed";

    @Bean
    public NewTopic waitEnqueueTopic() {
        return TopicBuilder.name(TOPIC_WAIT_ENQUEUE).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic waitAdmitTopic() {
        return TopicBuilder.name(TOPIC_WAIT_ADMIT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic seatHoldRequestedTopic() {
        return TopicBuilder.name(TOPIC_SEAT_HOLD_REQUESTED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic seatHeldTopic() {
        return TopicBuilder.name(TOPIC_SEAT_HELD).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic seatHoldFailedTopic() {
        return TopicBuilder.name(TOPIC_SEAT_HOLD_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentConfirmedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_CONFIRMED).partitions(3).replicas(1).build();
    }

    /**
     * ✅ JavaTimeModule 등 자동 등록
     * SeatHeldEvent(LocalDateTime) 같은 이벤트 역직렬화 안정화 목적
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    // ===== Producer =====
    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // VALUE_SERIALIZER는 아래에서 직접 설정

        DefaultKafkaProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(props);

        // ✅ JsonSerializer가 type headers를 넣을 수 있도록 ObjectMapper 연결
        JsonSerializer<Object> serializer = new JsonSerializer<>(objectMapper);
        serializer.setAddTypeInfo(true);  // 타입 헤더 강제
        pf.setValueSerializer(serializer);
        return pf;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    // ===== Consumer =====
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // VALUE_DESERIALIZER는 아래에서 직접 설정
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<Object> vd = new JsonDeserializer<>(objectMapper);

        // ✅ record 이벤트 패키지 신뢰
        vd.addTrustedPackages("com.skala.sktx.dto.event");

        // ✅ 타입 헤더 기반 역직렬화
        vd.setUseTypeHeaders(true);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), vd);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> f =
                new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }
}
