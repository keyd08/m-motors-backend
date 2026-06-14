package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.MonitoringStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class MonitoringController {

    @GetMapping("/api/monitoring/status")
    public MonitoringStatusResponse status() {
        return new MonitoringStatusResponse(
                "UP",
                "M-Motors Backend",
                "L'application est opérationnelle",
                LocalDateTime.now()
        );
    }
}
