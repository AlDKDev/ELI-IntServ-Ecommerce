package pe.edu.elitec.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException e, HttpServletRequest request) {

        logger.warn("🔍 Product not found: {}", e.getMessage());

        Map<String, Object> details = Map.of(
                "productId", e.getProductId() != null ? e.getProductId() : "unknown"
        );

        ErrorResponse error = new ErrorResponse(
                "PRODUCT_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException e, HttpServletRequest request) {

        logger.error("🔥 External service error: {}", e.getMessage(), e);

        Map<String, Object> details = Map.of(
                "service", e.getServiceName(),
                "statusCode", e.getStatusCode() > 0 ? e.getStatusCode() : "unknown"
        );

        ErrorResponse error = new ErrorResponse(
                "EXTERNAL_SERVICE_ERROR",
                "External service temporarily unavailable. Please try again later.",
                request.getRequestURI(),
                details
        );

        HttpStatus status = switch (e.getStatusCode()) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 400, 401, 403 -> HttpStatus.BAD_REQUEST;
            case 500, 502, 503 -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {

        logger.warn("⚠️ Invalid argument: {}", e.getMessage());

        ErrorResponse error = new ErrorResponse(
                "INVALID_ARGUMENT",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        logger.warn("🔄 Type mismatch for parameter '{}': {}", e.getName(), e.getMessage());

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                e.getValue(),
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"
        );

        Map<String, Object> details = Map.of(
                "parameter", e.getName(),
                "value", e.getValue() != null ? e.getValue().toString() : "null",
                "expectedType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse error = new ErrorResponse(
                "INVALID_PARAMETER",
                message,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        logger.warn("✅ Validation error: {}", e.getMessage());

        Map<String, Object> validationErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.validationError(
                "Request validation failed", validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e, HttpServletRequest request) {

        logger.error("💥 Unexpected error occurred", e);

        Map<String, Object> details = Map.of(
                "exception", e.getClass().getSimpleName(),
                "trace", e.getMessage() != null ? e.getMessage() : "No message available"
        );

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
