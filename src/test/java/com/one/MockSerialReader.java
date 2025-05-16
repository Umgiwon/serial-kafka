package com.one;

import com.one.serial.SerialReader;
import com.one.util.CRC16Util;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 실제 SerialPort 없이 테스트를 위한 Mock Reader
 */
public class MockSerialReader {

    private final SerialReader.DataHandler handler;

    public MockSerialReader(SerialReader.DataHandler handler) {
        this.handler = handler;
    }

    /**
     * 주기적으로 더미 데이터 만들어 전달
     */
    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            byte[] payload = new byte[]{0x01, 0x03, 0x02, 0x00, 0x7D}; // 장비 ID, Function, Byte Count, 값 125
            int crc = CRC16Util.calculateCRC16(payload, 0, payload.length);
            byte[] full = new byte[payload.length + 2];
            System.arraycopy(payload, 0, full, 0, payload.length);
            full[full.length - 2] = (byte) (crc & 0xFF); // CRC LSB
            full[full.length - 1] = (byte) ((crc >> 8) & 0xFF); // CRC MSB

            System.out.println("--------------테스트용 패킷 전송: " + bytesToHex(full));
            handler.handle(full);
        }, 0, 5, TimeUnit.SECONDS); // 5초마다 테스트 패킷 생성
    }

    private String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for(byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

}
