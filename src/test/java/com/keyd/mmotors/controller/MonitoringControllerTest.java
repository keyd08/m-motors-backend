package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.MonitoringStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringControllerTest {

    @Test
    @DisplayName("Doit retourner le statut de surveillance de l'application")
    void shouldReturnMonitoringStatus() {
        MonitoringController monitoringController = new MonitoringController();

        MonitoringStatusResponse result = monitoringController.status();

        assertThat(result.status()).isEqualTo("UP");
        assertThat(result.application()).isEqualTo("M-Motors Backend");
        assertThat(result.message()).contains("opérationnelle");
        assertThat(result.timestamp()).isNotNull();
    }
}
