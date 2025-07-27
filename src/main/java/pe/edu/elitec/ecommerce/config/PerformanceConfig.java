package pe.edu.elitec.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PerformanceConfig {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceConfig.class);


    @Bean
    public HealthIndicator fakeStoreHealthIndicator(RestTemplate restTemplate) {
        return () -> {
            try {
                long startTime = System.currentTimeMillis();

                restTemplate.getForEntity("https://fakestoreapi.com/products/1", Object.class);

                long responseTime = System.currentTimeMillis() - startTime;

                if (responseTime < 2000) {
                    return Health.up()
                            .withDetail("service", "FakeStore API")
                            .withDetail("responseTime", responseTime + "ms")
                            .withDetail("status", "HEALTHY")
                            .build();
                } else {
                    return Health.up()
                            .withDetail("service", "FakeStore API")
                            .withDetail("responseTime", responseTime + "ms")
                            .withDetail("status", "SLOW")
                            .build();
                }

            } catch (Exception e) {
                logger.warn("FakeStore API health check failed: {}", e.getMessage());
                return Health.down()
                        .withDetail("service", "FakeStore API")
                        .withDetail("error", e.getMessage())
                        .withDetail("status", "DOWN")
                        .build();
            }
        };
    }
}
