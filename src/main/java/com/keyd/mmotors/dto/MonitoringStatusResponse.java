package com.keyd.mmotors.dto;

import java.time.LocalDateTime;

public record MonitoringStatusResponse(
        String status,
        String application,
        String message,
        LocalDateTime timestamp
) {
}
