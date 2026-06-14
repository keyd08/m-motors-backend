package com.keyd.mmotors.exception;

import com.keyd.mmotors.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GlobalExceptionHandlerTest {

    private final AlertService alertService = mock(AlertService.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(alertService);

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Ressource introuvable");

        Map<String, Object> response = handler.handleResourceNotFound(exception);

        assertThat(response.get("status")).isEqualTo(404);
        assertThat(response.get("error")).isEqualTo("Not Found");
        assertThat(response.get("message")).isEqualTo("Ressource introuvable");
        assertThat(response.get("timestamp")).isNotNull();

        verify(alertService).notifyWarning("RESOURCE_NOT_FOUND", "Ressource introuvable");
    }

    @Test
    void shouldHandleBusinessRuleException() {
        BusinessRuleException exception = new BusinessRuleException("Règle métier invalide");

        Map<String, Object> response = handler.handleBusinessRuleException(exception);

        assertThat(response.get("status")).isEqualTo(400);
        assertThat(response.get("error")).isEqualTo("Business Rule Error");
        assertThat(response.get("message")).isEqualTo("Règle métier invalide");
        assertThat(response.get("timestamp")).isNotNull();

        verify(alertService).notifyWarning("BUSINESS_RULE", "Règle métier invalide");
    }

    @Test
    void shouldHandleValidationException() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "L'email est obligatoire"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        Map<String, Object> response = handler.handleValidationErrors(exception);

        assertThat(response.get("status")).isEqualTo(400);
        assertThat(response.get("error")).isEqualTo("Validation Error");
        assertThat(response.get("message")).isEqualTo("Les données envoyées ne sont pas valides");
        assertThat(response.get("timestamp")).isNotNull();
        assertThat(response.get("fields")).isInstanceOf(Map.class);

        verify(alertService).notifyWarning("VALIDATION_ERROR", "Les données envoyées ne sont pas valides");
    }

    @Test
    void shouldHandleUnexpectedException() {
        RuntimeException exception = new RuntimeException("Erreur inattendue");

        Map<String, Object> response = handler.handleUnexpectedError(exception);

        assertThat(response.get("status")).isEqualTo(500);
        assertThat(response.get("error")).isEqualTo("Internal Server Error");
        assertThat(response.get("message")).isEqualTo("Une erreur interne est survenue");
        assertThat(response.get("timestamp")).isNotNull();

        verify(alertService).notifyError("UNEXPECTED_ERROR", "Erreur interne inattendue", exception);
    }
}
