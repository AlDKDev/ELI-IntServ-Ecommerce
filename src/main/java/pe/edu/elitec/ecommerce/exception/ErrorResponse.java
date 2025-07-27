package pe.edu.elitec.ecommerce.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

//DTO para respuestas de error
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        String path,
        Map<String, Object> details
) {

    //Constructor para errores simples
    public ErrorResponse(String error, String message) {
        this(error, message, LocalDateTime.now(), null, null);
    }

    //Constructor con path
    public ErrorResponse(String error, String message, String path) {
        this(error, message, LocalDateTime.now(), path, null);
    }

    /**
     * Constructor completo con detalles
     */
    public ErrorResponse(String error, String message, String path, Map<String, Object> details) {
        this(error, message, LocalDateTime.now(), path, details);
    }

    /**
     * Factory method para errores de validación
     */
    public static ErrorResponse validationError(String message, Map<String, Object> validationDetails) {
        return new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now(), null, validationDetails);
    }

    /**
     * Factory method para errores de servicio externo
     */
    public static ErrorResponse externalServiceError(String serviceName, String message) {
        Map<String, Object> details = Map.of("service", serviceName);
        return new ErrorResponse("EXTERNAL_SERVICE_ERROR", message, LocalDateTime.now(), null, details);
    }
}