package pe.edu.elitec.ecommerce.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RestTemplateConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Autowired
    private RequestLoggingInterceptor loggingInterceptor;

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Value("${external.fakestore.timeout.connect}")
    private int connectTimeout;

    @Value("${external.fakestore.timeout.read}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(){

        logger.info("Configurando RestTemplate con interceptores multiples");

        /*
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();

        connectionManager.setMaxTotal(100);

        connectionManager.setDefaultMaxPerRoute(20);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);

        logger.info("RestTemplate configurado existosamente con connection pool y timeout");

        restTemplate.getInterceptors().add(new RequestLoggingInterceptor());

        logger.info("RestTemplate configurado exitosamente con interceptors");

        return restTemplate;*/

        PoolingHttpClientConnectionManager connectionManager = createConnectionManager();

        RequestConfig requestConfig = createRequestConfig();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);

        configureInterceptors(restTemplate);
        logger.info("RestTemplate configurado exitosamente con {} interceptores", restTemplate.getInterceptors().size());

        return restTemplate;
    }


    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        logger.info("Connection pool configurado: MaxTotal=100, MaxPerRoute=20");
        return connectionManager;
    }

    private RequestConfig createRequestConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        logger.info("Timeouts configurados - Connect: {}ms, Read: {}ms", connectTimeout, readTimeout);

        return requestConfig;
    }

    private void configureInterceptors(RestTemplate restTemplate) {
        logger.info("Configurando Interceptores en orden especifico....");

        List<ClientHttpRequestInterceptor> interceptors = Arrays.asList(
                authenticationInterceptor,
                loggingInterceptor
        );

        restTemplate.setInterceptors(interceptors);

        logger.info("Interceptores configurados");
        for(int i = 0; i < interceptors.size(); i++){
            logger.info("{}, {}", i + 1, interceptors.get(i).getClass().getSimpleName());

        }
    }
}
