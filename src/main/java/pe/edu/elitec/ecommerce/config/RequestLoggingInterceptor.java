package pe.edu.elitec.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private final View error;

    public RequestLoggingInterceptor(View error) {
        this.error = error;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        long startTime = System.currentTimeMillis();

        logDetailedRequest(request, body);

        //logger.debug("🚀 External API Request: {} {}", request.getMethod(), request.getURI());

        try {
            ClientHttpResponse response = execution.execute(request, body);

            long duration = System.currentTimeMillis() - startTime;

            /*logger.info("✅ External API Response: {} {} - Status: {} - Duration: {}ms",
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode().value(),
                    duration);*/
            logDetailedResponse(request, response, duration);

            return response;

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;

            /*logger.error("🔥 External API Error: {} {} - Duration: {}ms - Error: {}",
                    request.getMethod(),
                    request.getURI(),
                    duration,
                    e.getMessage());*/
            logDetailedError(request, e, duration);

            throw e;
        }
    }



    private void logDetailedRequest(HttpRequest request, byte[] body) {
        logger.info("=== EXTERNAL API REQUEST ===");
        logger.info("Method & URL: {} {}", request.getMethod(), request.getURI());
        logger.info("Headers: {}", request.getHeaders());

        if(body.length > 0) {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            logger.info("Request Body: {}", bodyString);
        }

        String correlationId = request.getHeaders().getFirst("X-Correlation-ID");
        if (correlationId!=null){
            logger.info("Correlational-ID: {}", correlationId);
        }

        logger.info("Request sent at: {}", System.currentTimeMillis());

    }


    private void logDetailedResponse(HttpRequest request, ClientHttpResponse response, long duration) throws IOException {
        logger.info("=== EXTERNAL API RESPONSE ===");
        logger.info("URL: {} {}", request.getMethod(), request.getURI());
        logger.info("Status Code: {} {}", response.getStatusCode().value(),response.getStatusText());
        logger.info("Response Headers: {}", response.getHeaders());
        logger.info("Duration: {}ms", duration);

        if(duration < 1000){
            logger.info("Performance: FAST (< 1s)");
        } else if (duration < 3000) {
            logger.warn("Performance: SLOW (1 -3s)");
        } else {
            logger.error("Performance: VERY SLOW (> 3s)");
        }

        logger.info("===============================");
    }

    private void logDetailedError(HttpRequest request, IOException error, long duration) {
        logger.error("=== EXTERNAL API ERROR ===");
        logger.error("URL: {} {}", request.getMethod(), request.getURI());
        logger.error("Duration before error: {}ms", duration);
        logger.error("Error Type: {}", error.getClass().getSimpleName());
        logger.error("Error Message: {}", error.getMessage());

        if(error.getMessage().contains("timeout")){
            logger.error("DIAGNOSIS: Timeout error - EXTERNAL API is slow");
        }else if (error.getMessage().contains("connection")){
            logger.error("DIAGNOSIS: Connection error - Network or DNS issue");
        }else {
            logger.error("DIAGNOSIS: Unknown error - Check error details above");
        }
        logger.info("===============================");
    }

}