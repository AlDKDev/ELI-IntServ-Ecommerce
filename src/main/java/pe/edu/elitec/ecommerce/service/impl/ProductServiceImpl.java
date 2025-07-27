package pe.edu.elitec.ecommerce.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pe.edu.elitec.ecommerce.dto.CategoryDTO;
import pe.edu.elitec.ecommerce.dto.ProductDTO;
import pe.edu.elitec.ecommerce.exception.ExternalServiceException;
import pe.edu.elitec.ecommerce.exception.ProductNotFoundException;
import pe.edu.elitec.ecommerce.service.ProductService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${external.fakestore.base-url}")
    private String baseUrl;

    private List<String> cachedCategories;

    @Override
    public List<ProductDTO> getAllProducts() {
        logger.info("🔍 Fetching all products from external API");

        try {
            String url = baseUrl + "/products";
            logger.debug("Making GET request to: {}", url);

            ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

            if (response.getBody() == null) {
                logger.warn("⚠️ Received null response body for all products");
                return List.of();
            }

            List<ProductDTO> products = Arrays.asList(response.getBody());
            logger.info("✅ Successfully fetched {} products", products.size());

            logProductStatistics(products);

            return products;

        } catch (ResourceAccessException e) {
            logger.error("🚫 Timeout or connection error while fetching products", e);
            throw new ExternalServiceException("FakeStore",
                    "Unable to connect to external service: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            logger.error("🔥 Server error from FakeStore API: {}", e.getStatusCode(), e);
            throw new ExternalServiceException("FakeStore", e.getStatusCode().value(),
                    "External service error: " + e.getMessage());

        } catch (Exception e) {
            logger.error("💥 Unexpected error while fetching products", e);
            throw new ExternalServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductDTO getProductById(Long id) {
        logger.info("🔍 Fetching product with id: {}", id);

        validateProductId(id);

        try {
            String url = baseUrl + "/products/" + id;
            logger.debug("Making GET request to: {}", url);

            ResponseEntity<ProductDTO> response = restTemplate.getForEntity(url, ProductDTO.class);

            ProductDTO product = response.getBody();
            if (product == null || product.id() == null) {
                logger.warn("⚠️ Product not found with id: {}", id);
                throw new ProductNotFoundException(id);
            }

            logger.info("✅ Successfully fetched product: {} ({})",
                    product.title(), product.getFormattedPrice());
            return product;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("🔍 Product not found with id: {}", id);
            throw new ProductNotFoundException(id);

        } catch (ResourceAccessException e) {
            logger.error("🚫 Timeout while fetching product {}", id, e);
            throw new ExternalServiceException("FakeStore",
                    "Unable to connect to external service: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            logger.error("🔥 Server error while fetching product {}: {}", id, e.getStatusCode(), e);
            throw new ExternalServiceException("FakeStore", e.getStatusCode().value(),
                    "External service error: " + e.getMessage());

        } catch (ProductNotFoundException e) {
            throw e;

        } catch (Exception e) {
            logger.error("💥 Unexpected error while fetching product {}", id, e);
            throw new ExternalServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ProductDTO> findProductById(Long id) {
        try {
            return Optional.of(getProductById(id));
        } catch (ProductNotFoundException e) {
            logger.debug("Product {} not found, returning empty Optional", id);
            return Optional.empty();
        }
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        logger.info("🔍 Fetching all categories from external API");

        try {
            String url = baseUrl + "/products/categories";
            logger.debug("Making GET request to: {}", url);

            ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

            if (response.getBody() == null) {
                logger.warn("⚠️ Received null response for categories");
                return List.of();
            }

            List<CategoryDTO> categories = Arrays.stream(response.getBody())
                    .map(CategoryDTO::from)
                    .collect(Collectors.toList());

            cachedCategories = Arrays.asList(response.getBody());

            logger.info("✅ Successfully fetched {} categories: {}",
                    categories.size(),
                    categories.stream().map(CategoryDTO::name).collect(Collectors.joining(", ")));

            return categories;

        } catch (ResourceAccessException e) {
            logger.error("🚫 Timeout while fetching categories", e);
            throw new ExternalServiceException("FakeStore",
                    "Unable to connect to external service: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            logger.error("🔥 Server error while fetching categories: {}", e.getStatusCode(), e);
            throw new ExternalServiceException("FakeStore", e.getStatusCode().value(),
                    "External service error: " + e.getMessage());

        } catch (Exception e) {
            logger.error("💥 Unexpected error while fetching categories", e);
            throw new ExternalServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String category) {
        logger.info("🔍 Fetching products for category: '{}'", category);

        validateCategory(category);

        try {
            String url = baseUrl + "/products/category/" + category.trim();
            logger.debug("Making GET request to: {}", url);

            ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

            if (response.getBody() == null) {
                logger.warn("⚠️ Received null response for category: {}", category);
                return List.of();
            }

            List<ProductDTO> products = Arrays.asList(response.getBody());
            logger.info("✅ Successfully fetched {} products for category: '{}'",
                    products.size(), category);

            return products;

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("🔍 Category not found: '{}', returning empty list", category);
            return List.of();

        } catch (ResourceAccessException e) {
            logger.error("🚫 Timeout while fetching products for category '{}'", category, e);
            throw new ExternalServiceException("FakeStore",
                    "Unable to connect to external service: " + e.getMessage());

        } catch (HttpServerErrorException e) {
            logger.error("🔥 Server error while fetching products for category '{}': {}",
                    category, e.getStatusCode(), e);
            throw new ExternalServiceException("FakeStore", e.getStatusCode().value(),
                    "External service error: " + e.getMessage());

        } catch (Exception e) {
            logger.error("💥 Unexpected error while fetching products for category '{}'", category, e);
            throw new ExternalServiceException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductDTO> getProductsWithLimit(int limit) {
        logger.info("🔍 Fetching products with limit: {}", limit);

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        try {
            String url = baseUrl + "/products?limit=" + limit;
            logger.debug("Making GET request to: {}", url);

            ResponseEntity<ProductDTO[]> response = restTemplate.getForEntity(url, ProductDTO[].class);

            if (response.getBody() == null) {
                return List.of();
            }

            List<ProductDTO> products = Arrays.asList(response.getBody());
            logger.info("✅ Successfully fetched {} products with limit {}", products.size(), limit);

            return products;

        } catch (Exception e) {
            logger.error("💥 Error fetching products with limit {}", limit, e);
            throw new ExternalServiceException("Error fetching limited products: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean categoryExists(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        try {
            if (cachedCategories != null) {
                return cachedCategories.contains(category.trim().toLowerCase());
            }

            List<CategoryDTO> categories = getAllCategories();
            return categories.stream()
                    .anyMatch(cat -> cat.name().equals(category.trim().toLowerCase()));

        } catch (Exception e) {
            logger.warn("Error checking if category '{}' exists: {}", category, e.getMessage());
            return false;
        }
    }

    private void validateProductId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
    }

    private void logProductStatistics(List<ProductDTO> products) {
        if (products.isEmpty()) {
            return;
        }

        try {
            long premiumCount = products.stream()
                    .filter(ProductDTO::isPremium)
                    .count();

            long categoriesCount = products.stream()
                    .map(ProductDTO::category)
                    .distinct()
                    .count();

            logger.info("📊 Product Statistics: {} total, {} premium products, {} categories",
                    products.size(), premiumCount, categoriesCount);

        } catch (Exception e) {
            logger.debug("Could not calculate product statistics: {}", e.getMessage());
        }
    }
}