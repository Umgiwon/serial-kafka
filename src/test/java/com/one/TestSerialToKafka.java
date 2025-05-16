package com.one;

import com.one.kafka.KafkaSender;
import com.one.util.CRC16Util;

/**
 * MockSerialReader -> CRC 검사 -> Kafka 전송 통합 테스트
 */
public class TestSerialToKafka {
    public static void main(String[] args) {

        // kafka sender 초기화
        KafkaSender kafkaSender;
        try {
            kafkaSender = new KafkaSender("src/main/java/com/one/config/kafka.properties");
        } catch (Exception e) {
            System.err.println("Kafka 설정 로딩 실패 : " + e.getMessage());
            return;
        }

        // CRC 검증 후 Kafka로 전송
        MockSerialReader mockReader = new MockSerialReader(data -> {
            if(data != null && data.length > 3 && CRC16Util.isValidCRC(data)) {
                kafkaSender.send(data);
            } else {
                System.err.println("CRC 검증 실패, 전송 안함");
            }
        });

        // 테스트 시작
        mockReader.start();

        // 종료 시 KafkaSender 정리
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaSender::close));
    }
}

