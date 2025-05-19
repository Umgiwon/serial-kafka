package com.one;

import com.one.serial.SerialReader;
import com.one.util.CRC16Util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 실제 SerialPort 없이 테스트를 위한 Mock Reader
 */
public class MockSerialReader implements AutoCloseable {

    private final SerialReader.DataHandler handler;
    private final ScheduledExecutorService executor;


    public MockSerialReader(SerialReader.DataHandler handler) {
        this.handler = handler;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 주기적으로 더미 데이터 만들어 전달
     */
    public void start() {
        executor.scheduleAtFixedRate(() -> {

            // 8바이트 패킷 구성
            byte[] payload = new byte[]{
                    0x01,       // 장비 ID
                    0x03,       // Function
                    0x02,       // Byte Count
                    0x00, 0x7D, // 데이터 값 (125)
                    0x00        // 추가 데이터
            };
            int crc = CRC16Util.calculateCRC16(payload, 0, payload.length);
            byte[] full = new byte[payload.length + 2];
            System.arraycopy(payload, 0, full, 0, payload.length);
            full[full.length - 2] = (byte) (crc & 0xFF); // CRC LSB
            full[full.length - 1] = (byte) ((crc >> 8) & 0xFF); // CRC MSB

            System.out.println("--------------테스트용 패킷 전송: " + bytesToHex(full));
            handler.handle(full);
        }, 0, 5, TimeUnit.SECONDS); // 5초마다 테스트 패킷 생성
    }

    @Override
    public void close() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for(byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

}
