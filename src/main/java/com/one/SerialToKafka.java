package com.one;

import com.one.kafka.KafkaSender;
import com.one.serial.SerialReader;
import com.one.util.CRC16Util;
import com.one.util.PortDetectorUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * SerialRead를 통해 데이터를 읽고, Kafka로 전송
 */
@Slf4j
public class SerialToKafka {

    // FIXME 운영 환경에 맞게 수정
//    private static final String PORT_NAME = PortDetectorUtil.detectAvailablePort(); // 포트 이름 ex) "COM3" (Window) or "/dev/ttyUSB0" (Linux)
    private static final int BAUD_RATE = 9600; // 보레이트

    public static void main(String[] args) {

//        // 1. SerialReader 초기화
//        SerialReader reader = new SerialReader();
//
//        // 포트 초기화 및 열기
//        if(!reader.initialize(PORT_NAME, BAUD_RATE)) {
//            log.error("시리얼 포트를 열 수 없습니다.");
//            return;
//        }
//
//        // 2. KafkaSender 초기화
//        KafkaSender kafkaSender;
//        try {
//            kafkaSender = new KafkaSender("src/main/java/com/one/config/kafka.properties");
//        } catch (Exception e) {
//            log.error("Kafka 설정 파일 로드 실패: {}", e.getMessage());
//            System.err.println("Kafka 설정 파일 로드 실패: " + e.getMessage());
//            reader.close();
//            return;
//        }
//
//        // 3. 데이터 수신 시 Kafka로 전송
//        reader.readLoop(data -> {
//
//            // 데이터가 유효한 경우에만 전송
//            if(data != null && data.length > 3 && CRC16Util.isValidCRC(data)) {
//                kafkaSender.send(data);
//            } else {
//                log.error("CRC 검증 실패. 데이터 무시됨.");
//            }
//        });
//
//        // 4. 종료 시 자원 정리
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            reader.close();
//            kafkaSender.close();
//        }));

        // 1. 포트 초기화 및 열기
        String portName = PortDetectorUtil.detectAvailablePort();
        if (portName == null) {
            log.error("사용 가능한 시리얼 포트를 찾을 수 없습니다.");
            return;
        }

        try (SerialReader reader = new SerialReader();
             KafkaSender kafkaSender = new KafkaSender("kafka.properties")) {

            if (!reader.initialize(portName, BAUD_RATE)) {
                log.error("시리얼 포트를 열 수 없습니다: {}", portName);
                return;
            }

            // 2. 데이터 수신 시 Kafka로 전송
            reader.readLoop(data -> {

                if (data != null && data.length > 3 && CRC16Util.isValidCRC(data)) {
                    try {
                        kafkaSender.send(data);
                    } catch (Exception e) {
                        log.error("Kafka 전송 중 오류 발생: {}", e.getMessage(), e);
                    }
                } else {
                    log.error("CRC 검증 실패. 데이터 무시됨.");
                }
            });

            // 메인 스레드 대기
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("프로그램 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}