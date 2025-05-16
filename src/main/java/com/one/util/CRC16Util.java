package com.one.util;

/**
 * CRC16-MODBUS 계산 유틸리티 클래스
 * - Modbus RTU 포맷에서 사용되는 CRC16 알고리즘을 기반으로 동작
 * - 데이터 끝 2바이트를 CRC 로 보고, 계산된 CRC 와 비교하여 유효성 검증 수행
 */
public class CRC16Util {

    /**
     * 주어진 데이터가 올바른 CRC 를 포함하고 있는지 검증
     * 데이터 끝의 2바이트는 CRC (LSB first)로 간주함.
     * @param data 전체 데이터 (CRC 포함)
     * @return
     */
    public static boolean isValidCRC(byte[] data) {

        // 데이터가 너무 짧아 CRC 검사 불가
        if(data == null || data.length < 3) {
            return false;
        }

        int length = data.length;

        // 데이터 마지막 2바이트에서 예상 CRC 추출 (LSB 먼저 : [low][high])
        int expectedCRC = ((data[length - 1] & 0xFF) << 8) | (data[length - 2] & 0xFF);

        // 실제 데이터에 대해 CRC 계산 (CRC 바이트 전까지)
        int calculatedCRC = calculateCRC16(data, 0, length - 2);

        return expectedCRC == calculatedCRC;
    }

    /**
     * CRC-16 (Modbus RTU) 알고리즘으로 CRC 계산
     * @param data 바이트 배열
     * @param offset 시작 위치
     * @param length 계산할 길이
     * @return 16비트 CRC 값
     */
    public static int calculateCRC16(byte[] data, int offset, int length) {

        int crc = 0xFFFF; // 초기값

        for(int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF); // 현재 바이트와 XOR

            // 각 비트를 순차적으로 시프트하며 연산
            for(int j = 0; j < 8; j++) {

                boolean lsb = (crc & 0x0001) != 0;
                crc >>>= 1;
                if(lsb) crc ^= 0xA001; // Modbus RTU 다항식
            }
        }

        return crc;
    }
}
