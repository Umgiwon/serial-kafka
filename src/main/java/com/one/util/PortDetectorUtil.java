package com.one.util;

import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortDetectorUtil {

    /**
     * 연결 가능한 포트 자동 탐지(window, linux, macOS 전부 가능)
     * @return 연결 가능한 포트 중 첫번째 포트를 반환한다.
     */
    public static String detectAvailablePort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        log.info("사용 가능한 시리얼 포트 목록:");
        for(SerialPort port : ports) {
            log.info("  - {} ({})", port.getSystemPortName(), port.getDescriptivePortName());
        }

        if(ports.length > 0) {
            // 첫번째 사용 가능한 포트 반환
            return ports[0].getSystemPortName();
        } else {
            log.error("사용 가능한 시리얼 포트를 찾을 수 없습니다.");
            return null;
        }
    }
}
