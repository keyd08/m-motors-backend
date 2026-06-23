package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.MonitoringAlertResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AlertService {

    public MonitoringAlertResponse notifyWarning(String source, String message) {
        MonitoringAlertResponse alert = new MonitoringAlertResponse(
                "WARNING",
                source,
                message,
                LocalDateTime.now()
        );

        log.warn("[ALERT][{}] {}", source, message);

        return alert;
    }

    public MonitoringAlertResponse notifyError(String source, String message, Throwable exception) {
        MonitoringAlertResponse alert = new MonitoringAlertResponse(
                "ERROR",
                source,
                message,
                LocalDateTime.now()
        );

        log.error("[ALERT][{}] {}", source, message, exception);

        return alert;
    }
}
