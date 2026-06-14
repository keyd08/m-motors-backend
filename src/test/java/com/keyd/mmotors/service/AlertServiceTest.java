package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.MonitoringAlertResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertServiceTest {

    private final AlertService alertService = new AlertService();

    @Test
    @DisplayName("Doit créer une alerte de niveau WARNING")
    void shouldCreateWarningAlert() {
        MonitoringAlertResponse alert = alertService.notifyWarning(
                "TEST",
                "Message d'avertissement"
        );

        assertThat(alert.level()).isEqualTo("WARNING");
        assertThat(alert.source()).isEqualTo("TEST");
        assertThat(alert.message()).isEqualTo("Message d'avertissement");
        assertThat(alert.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Doit créer une alerte de niveau ERROR")
    void shouldCreateErrorAlert() {
        RuntimeException exception = new RuntimeException("Erreur test");

        MonitoringAlertResponse alert = alertService.notifyError(
                "TEST",
                "Message d'erreur",
                exception
        );

        assertThat(alert.level()).isEqualTo("ERROR");
        assertThat(alert.source()).isEqualTo("TEST");
        assertThat(alert.message()).isEqualTo("Message d'erreur");
        assertThat(alert.timestamp()).isNotNull();
    }
}
