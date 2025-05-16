package com.one.serial;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Serial Port를 통해 데이터를 읽어오는 클래스
 */
@Slf4j
public class SerialReader {

    private SerialPort serialPort;

    /**
     * 시리얼 포트를 초기화 하고 연다.
     * @param portDescriptor 사용할 포트 이름 (예: "COM3", "/dev/ttyUSB0")"
     * @param baudRate 보레이트 (예: 9600, 19200 등)
     * @return 포트 열기 성공 여부
     */
    public boolean initialize(String portDescriptor, int baudRate) {

        serialPort = SerialPort.getCommPort(portDescriptor);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY); // 포트 설정
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0); // 읽기 타임아웃 설정

        // 포트 열기
        boolean opened = serialPort.openPort();
        log.info("포트 열기 {} : {}", (opened ? "성공" : "실패"), portDescriptor);

        return opened;
    }

    /**
     * 포트로부터 데이터를 반복적으로 읽고, 일긍ㄴ 데이터를 핸들러로 전달
     * @param handler 데이터 수신 후 처리할 콜백 핸들러
     */
    public void readLoop(DataHandler handler) {

        try (InputStream in = serialPort.getInputStream()) {

            byte[] buffer = new byte[256]; // 최대 256byte 버퍼

            while (true) {

                // 데이터 읽기
                int numRead = in.read(buffer);

                if(numRead > 0) {

                    // 읽은 데이터만 잘라서 새로운 배열 생성
                    byte[] data = new byte[numRead];
                    System.arraycopy(buffer, 0, data, 0, numRead);

                    // 로그 출력 (16진수로 변환해서 보여준다)
                    log.info("수신된 데이터 ({}바이트): {}", numRead, bytesToHex(data));

                    // 수신된 데이터 핸들링
                    handler.handle(data);
                }
            }
        } catch (IOException e) {
            log.error("데이터 수신 중 오류", e);
        }
    }

    /**
     * 포트를 닫는다.
     */
    public void close() {

        if(serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.info("포트 닫힘");
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     * @param bytes
     * @return
     */
    private String bytesToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }

    /**
     * 데이터를 처리하기 위한 핸들러 인터페이스
     */
    public interface DataHandler {
        void handle(byte[] data);
    }
}
