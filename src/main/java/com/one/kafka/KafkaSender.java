package com.one.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Kafka로 데이터를 전송하는 클래스
 */
@Slf4j
public class KafkaSender implements AutoCloseable {

    private final KafkaProducer<String, byte[]> producer;
    private final String topic;

    /**
     * KafkaSender 생성자
     * @param propertiesPath Kafka 설정파일 경로 ex) "kafka.properties"
     * @throws IOException 설정 파일 로드 실패 시 예외 발생
     */
    public KafkaSender(String propertiesPath) throws IOException {
        Properties props = loadAndValidateProperties(propertiesPath);
        this.producer = new KafkaProducer<>(props);
        this.topic = props.getProperty("topic.name");
        log.info("KafkaSender 초기화 완료 - topic: {}", topic);
    }

    /**
     * 설정 파일
     * @param propertiesPath Kafka 설정파일 경로 ex) "kafka.properties"
     * @return
     * @throws IOException
     */
    private Properties loadAndValidateProperties(String propertiesPath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(propertiesPath)) {
            props.load(input);
        }

        // 필수 설정 검증
        String[] requiredProps = {"bootstrap.servers", "topic.name"};
        for (String prop : requiredProps) {
            if (!props.containsKey(prop)) {
                throw new IllegalArgumentException("필수 설정 누락: " + prop);
            }
        }

        // Kafka Producer 기본 설정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return props;
    }

    /**
     * Kafka로 데이터 전송 (비동기)
     * @param data 전송할 바이트 배열
     */
    public void send(byte[] data) {

        if (data == null) {
            throw new IllegalArgumentException("데이터가 null입니다");
        }

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, data);
        producer.send(record, (metadata, exception) -> {
            if(exception != null) {
                log.error("Kafka 전송 실패", exception);
            } else {
                log.info("Kafka 전송 성공 - topic: {}, partition: {}, offset: {}"
                        , metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Kafka Producer 종료 및 자원 정리
     */
    @Override
    public void close() {
        try {
            producer.flush(); // 남아있는 메시지 전송
        } finally {
            producer.close();
            log.info("Kafka Producer 종료");
        }
    }
}
