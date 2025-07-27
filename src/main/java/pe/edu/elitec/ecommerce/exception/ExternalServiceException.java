package pe.edu.elitec.ecommerce.exception;

//Exception para errores en servicios externos
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String message) {
        super(message);
        this.serviceName = "Unknown";
        this.statusCode = 0;
    }

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("[%s] %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = 0;
    }

    public ExternalServiceException(String serviceName, int statusCode, String message) {
        super(String.format("[%s] HTTP %d: %s", serviceName, statusCode, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = "Unknown";
        this.statusCode = 0;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
