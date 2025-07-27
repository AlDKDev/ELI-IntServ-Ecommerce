package pe.edu.elitec.ecommerce.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.elitec.ecommerce.dto.CategoryDTO;
import pe.edu.elitec.ecommerce.dto.ProductDTO;
import pe.edu.elitec.ecommerce.service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        logger.info("🌐 GET /api/v1/products - Fetching all products");

        long startTime = System.currentTimeMillis();

        try {
            List<ProductDTO> products = productService.getAllProducts();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Returning {} products in {}ms", products.size(), duration);

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(products.size()))
                    .header("X-Response-Time", duration + "ms")
                    .body(products);

        } catch (Exception e) {
            logger.error("💥 Error fetching all products", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @PathVariable
            @Min(value = 1, message = "Product ID must be positive")
            Long id) {

        logger.info("🔍 GET /api/v1/products/{} - Fetching product by ID", id);

        long startTime = System.currentTimeMillis();

        try {
            ProductDTO product = productService.getProductById(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Returning product: '{}' in {}ms", product.title(), duration);

            return ResponseEntity.ok()
                    .header("X-Product-ID", String.valueOf(product.id()))
                    .header("X-Product-Category", product.category())
                    .header("X-Response-Time", duration + "ms")
                    .body(product);

        } catch (Exception e) {
            logger.error("💥 Error fetching product {}", id, e);
            throw e;
        }
    }

    @GetMapping("/safe/{id}")
    public ResponseEntity<ProductDTO> getProductSafely(
            @PathVariable @Min(value = 1, message = "Product ID must be positive") Long id) {

        logger.info("🛡️ GET /api/v1/products/safe/{} - Safe product fetch", id);

        Optional<ProductDTO> product = productService.findProductById(id);

        if (product.isPresent()) {
            logger.info("✅ Product found: {}", product.get().title());
            return ResponseEntity.ok(product.get());
        } else {
            logger.info("🔍 Product {} not found, returning 204 No Content", id);
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        logger.info("📂 GET /api/v1/products/categories - Fetching all categories");

        long startTime = System.currentTimeMillis();

        try {
            List<CategoryDTO> categories = productService.getAllCategories();

            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ Returning {} categories in {}ms: {}",
                    categories.size(), duration,
                    categories.stream().map(CategoryDTO::name).toList());

            return ResponseEntity.ok()
                    .header("X-Total-Categories", String.valueOf(categories.size()))
                    .header("X-Response-Time", duration + "ms")
                    .body(categories);

        } catch (Exception e) {
            logger.error("💥 Error fetching categories", e);
            throw e;
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @PathVariable
            @NotBlank(message = "Category cannot be blank")
            String category) {

        logger.info("📂 GET /api/v1/products/category/{} - Fetching products by category", category);

        long startTime = System.currentTimeMillis();

        try {
            List<ProductDTO> products = productService.getProductsByCategory(category);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Returning {} products for category '{}' in {}ms",
                    products.size(), category, duration);

            return ResponseEntity.ok()
                    .header("X-Category", category)
                    .header("X-Total-Count", String.valueOf(products.size()))
                    .header("X-Response-Time", duration + "ms")
                    .body(products);

        } catch (Exception e) {
            logger.error("💥 Error fetching products for category '{}'", category, e);
            throw e;
        }
    }

    @GetMapping("/limited")
    public ResponseEntity<List<ProductDTO>> getProductsWithLimit(
            @RequestParam(value = "limit", defaultValue = "10")
            @Min(value = 1, message = "Limit must be at least 1")
            Integer limit) {

        logger.info("🔢 GET /api/v1/products/limited?limit={} - Fetching limited products", limit);

        long startTime = System.currentTimeMillis();

        try {
            List<ProductDTO> products = productService.getProductsWithLimit(limit);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Returning {} products (limit: {}) in {}ms",
                    products.size(), limit, duration);

            return ResponseEntity.ok()
                    .header("X-Requested-Limit", String.valueOf(limit))
                    .header("X-Actual-Count", String.valueOf(products.size()))
                    .header("X-Response-Time", duration + "ms")
                    .body(products);

        } catch (Exception e) {
            logger.error("💥 Error fetching products with limit {}", limit, e);
            throw e;
        }
    }

    @GetMapping("/category/{category}/exists")
    public ResponseEntity<Map<String, Object>> checkCategoryExists(
            @PathVariable @NotBlank(message = "Category cannot be blank") String category) {

        logger.info("🔍 GET /api/v1/products/category/{}/exists - Checking category existence", category);

        boolean exists = productService.categoryExists(category);

        Map<String, Object> response = Map.of(
                "category", category,
                "exists", exists,
                "timestamp", System.currentTimeMillis()
        );

        logger.info("✅ Category '{}' exists: {}", category, exists);

        return ResponseEntity.ok()
                .header("X-Category-Check", category)
                .body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        logger.info("📊 GET /api/v1/products/stats - Fetching product statistics");

        long startTime = System.currentTimeMillis();

        try {
            List<ProductDTO> allProducts = productService.getAllProducts();
            List<CategoryDTO> allCategories = productService.getAllCategories();

            long premiumCount = allProducts.stream()
                    .filter(ProductDTO::isPremium)
                    .count();

            double averagePrice = allProducts.stream()
                    .filter(p -> p.price() != null)
                    .mapToDouble(p -> p.price().doubleValue())
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = Map.of(
                    "totalProducts", allProducts.size(),
                    "totalCategories", allCategories.size(),
                    "premiumProducts", premiumCount,
                    "averagePrice", Math.round(averagePrice * 100.0) / 100.0,
                    "premiumPercentage", allProducts.isEmpty() ? 0 :
                            Math.round((premiumCount * 100.0 / allProducts.size()) * 100.0) / 100.0,
                    "timestamp", System.currentTimeMillis()
            );

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Generated statistics in {}ms: {} products, {} categories",
                    duration, allProducts.size(), allCategories.size());

            return ResponseEntity.ok()
                    .header("X-Response-Time", duration + "ms")
                    .body(stats);

        } catch (Exception e) {
            logger.error("💥 Error generating product statistics", e);
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("❤️ GET /api/v1/products/health - Controller health check");

        try {
            List<ProductDTO> testProducts = productService.getProductsWithLimit(1);

            Map<String, Object> health = Map.of(
                    "status", "UP",
                    "controller", "ProductController",
                    "externalApiConnectivity", !testProducts.isEmpty() ? "UP" : "DOWN",
                    "timestamp", System.currentTimeMillis()
            );

            logger.info("✅ Controller health check passed");
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("💥 Controller health check failed", e);

            Map<String, Object> health = Map.of(
                    "status", "DOWN",
                    "controller", "ProductController",
                    "externalApiConnectivity", "DOWN",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}