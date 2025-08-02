package pe.edu.elitec.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;


public class AuthenticationInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Value("${external.fakestore.api.key:demo-api-key-123}")
    private String apiKey;

    @Value("${spring.application.name:ecommerce}")
    private String applicationName;



    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        logger.debug("AuthenticationIntercetor: Adding authentication headers");

        addAuthenticationHeaders(request);

        addCorrelationId(request);

        addIdentificationHeaders(request);

        logger.debug("Authenticatiom headers added successfully");

        return execution.execute(request, body);
    }

    private void addAuthenticationHeaders(HttpRequest request) {
        request.getHeaders().add("Authorization", "Bearer" + apiKey);
        request.getHeaders().add("X-API-Key", apiKey);
        request.getHeaders().add("X-Auth-Type", "APIKEY");

        logger.debug("🔑 Added API Key: {} (masked)", maskApiKey(apiKey));
    }



    private void addCorrelationId(HttpRequest request) {
        String correlationId = UUID.randomUUID().toString().substring(0,8);

        request.getHeaders().add("X-Correlation-ID", correlationId);
        request.getHeaders().add("X-Request-ID", correlationId);

        logger.debug("Added Correlation-ID: {}", correlationId);
    }

    private void addIdentificationHeaders(HttpRequest request) {
        request.getHeaders().add("X-Client-Name", applicationName);
        request.getHeaders().add("X-Client-Version", "1.0.0");
        request.getHeaders().add("X-Client-type", "SPRING_BOOT");

        request.getHeaders().add("User-Agent", applicationName + "/1.0.0 (Spring Boot RestTemplate");

        logger.debug("Added client identification headers");
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8){
            return "***";
        }
        return apiKey.substring(0,4) + "***" + apiKey.substring(apiKey.length()-4);
    }

}
