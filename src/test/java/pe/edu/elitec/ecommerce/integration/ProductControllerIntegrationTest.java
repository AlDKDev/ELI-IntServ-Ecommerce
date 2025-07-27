package pe.edu.elitec.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.elitec.ecommerce.dto.ProductDTO;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ProductControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/products";
    }

    @Test
    @DisplayName("01. Health Check - Controller debe estar UP")
    void testControllerHealthCheck() {

        String url = getBaseUrl() + "/health";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("ProductController", response.getBody().get("controller"));

        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("02. Get All Products - Debe retornar lista de productos")
    void testGetAllProducts() {

        String url = getBaseUrl();

        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        assertNotNull(response.getHeaders().get("X-Total-Count"));
        assertNotNull(response.getHeaders().get("X-Response-Time"));

        ProductDTO firstProduct = response.getBody()[0];
        assertNotNull(firstProduct.id());
        assertNotNull(firstProduct.title());
        assertNotNull(firstProduct.price());
        assertNotNull(firstProduct.category());

        System.out.println("✅ Found " + response.getBody().length + " products");
    }

    @Test
    @DisplayName("03. Get Product By ID - Debe retornar producto específico")
    void testGetProductById() {

        Long productId = 1L;
        String url = getBaseUrl() + "/" + productId;

        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(url, ProductDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(productId, response.getBody().id());

        assertEquals(productId.toString(), response.getHeaders().getFirst("X-Product-ID"));
        assertNotNull(response.getHeaders().getFirst("X-Product-Category"));
        assertNotNull(response.getHeaders().getFirst("X-Response-Time"));

        System.out.println("✅ Found product: " + response.getBody().title());
    }

    @Test
    @DisplayName("04. Get Product Safe - No existente debe retornar 204")
    void testGetProductSafeNotFound() {

        Long nonExistentId = 999999L;
        String url = getBaseUrl() + "/safe/" + nonExistentId;

        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(url, ProductDTO.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        System.out.println("✅ Safe endpoint correctly returns 204 for non-existent product");
    }

    @Test
    @DisplayName("05. Get Categories - Debe retornar lista de categorías")
    void testGetAllCategories() {

        String url = getBaseUrl() + "/categories";

        ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 4);

        assertNotNull(response.getHeaders().get("X-Total-Categories"));
        assertNotNull(response.getHeaders().get("X-Response-Time"));

        System.out.println("✅ Found " + response.getBody().length + " categories");
    }

    @Test
    @DisplayName("06. Get Products By Category - Debe filtrar por categoría")
    void testGetProductsByCategory() {

        String category = "electronics";
        String url = getBaseUrl() + "/category/" + category;

        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        for (ProductDTO product : response.getBody()) {
            assertEquals(category, product.category());
        }

        assertEquals(category, response.getHeaders().getFirst("X-Category"));
        assertNotNull(response.getHeaders().getFirst("X-Total-Count"));

        System.out.println("✅ Found " + response.getBody().length + " products in category: " + category);
    }

    @Test
    @DisplayName("07. Get Products With Limit - Debe respetar límite")
    void testGetProductsWithLimit() {

        int limit = 3;
        String url = getBaseUrl() + "/limited?limit=" + limit;

        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length <= limit);

        assertEquals(String.valueOf(limit), response.getHeaders().getFirst("X-Requested-Limit"));
        assertEquals(String.valueOf(response.getBody().length), response.getHeaders().getFirst("X-Actual-Count"));

        System.out.println("✅ Limit respected: requested=" + limit + ", actual=" + response.getBody().length);
    }

    @Test
    @DisplayName("08. Check Category Exists - Debe verificar existencia")
    void testCheckCategoryExists() {

        String category = "electronics";
        String url = getBaseUrl() + "/category/" + category + "/exists";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(category, response.getBody().get("category"));
        assertEquals(true, response.getBody().get("exists"));

        System.out.println("✅ Category existence check works correctly");
    }

    @Test
    @DisplayName("09. Get Statistics - Debe retornar estadísticas válidas")
    void testGetProductStatistics() {

        String url = getBaseUrl() + "/stats";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().containsKey("totalProducts"));
        assertTrue(response.getBody().containsKey("totalCategories"));
        assertTrue(response.getBody().containsKey("premiumProducts"));
        assertTrue(response.getBody().containsKey("averagePrice"));
        assertTrue(response.getBody().containsKey("premiumPercentage"));

        assertTrue((Integer) response.getBody().get("totalProducts") > 0);
        assertTrue((Integer) response.getBody().get("totalCategories") > 0);
        assertTrue((Double) response.getBody().get("averagePrice") > 0);

        System.out.println("✅ Statistics: " + response.getBody());
    }

    @Test
    @DisplayName("10. Error Handling - Product Not Found debe retornar 404")
    void testProductNotFoundError() {

        Long nonExistentId = 999999L;
        String url = getBaseUrl() + "/" + nonExistentId;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PRODUCT_NOT_FOUND", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains(nonExistentId.toString()));

        System.out.println("✅ Error handling works correctly for 404");
    }

    @Test
    @DisplayName("11. Error Handling - Invalid ID debe retornar 400")
    void testInvalidIdError() {

        String invalidId = "invalid-id";
        String url = getBaseUrl() + "/" + invalidId;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().get("error"));

        System.out.println("✅ Error handling works correctly for 400");
    }

    @Test
    @DisplayName("12. Performance Test - Response times should be reasonable")
    void testPerformance() {

        String url = getBaseUrl();

        long startTime = System.currentTimeMillis();
        ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(responseTime < 5000, "Response time should be less than 5 seconds, was: " + responseTime + "ms");

        String headerResponseTime = response.getHeaders().getFirst("X-Response-Time");
        assertNotNull(headerResponseTime);
        assertTrue(headerResponseTime.endsWith("ms"));

        System.out.println("✅ Performance test passed: " + responseTime + "ms (header: " + headerResponseTime + ")");
    }
}