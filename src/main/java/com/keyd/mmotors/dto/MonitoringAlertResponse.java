package com.keyd.mmotors.dto;

import java.time.LocalDateTime;

public record MonitoringAlertResponse(
        String level,
        String source,
        String message,
        LocalDateTime timestamp
) {
}
