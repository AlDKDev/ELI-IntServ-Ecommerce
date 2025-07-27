package pe.edu.elitec.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        long startTime = System.currentTimeMillis();

        logger.debug("🚀 External API Request: {} {}", request.getMethod(), request.getURI());

        try {
            ClientHttpResponse response = execution.execute(request, body);

            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ External API Response: {} {} - Status: {} - Duration: {}ms",
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode().value(),
                    duration);

            return response;

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("🔥 External API Error: {} {} - Duration: {}ms - Error: {}",
                    request.getMethod(),
                    request.getURI(),
                    duration,
                    e.getMessage());

            throw e;
        }
    }
}