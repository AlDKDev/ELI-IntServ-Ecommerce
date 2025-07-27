package pe.edu.elitec.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.edu.elitec.ecommerce.dto.ProductDTO;
import pe.edu.elitec.ecommerce.exception.ProductNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @Test
    void getAllProducts_ShouldReturnProductsList() {
        // When
        List<ProductDTO> products = productService.getAllProducts();

        // Then
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.size() > 0);

        // Verify first product structure
        ProductDTO firstProduct = products.get(0);
        assertNotNull(firstProduct.id());
        assertNotNull(firstProduct.title());
        assertNotNull(firstProduct.price());
        assertNotNull(firstProduct.category());
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() {
        // Given
        Long productId = 1L;

        // When
        ProductDTO product = productService.getProductById(productId);

        // Then
        assertNotNull(product);
        assertEquals(productId, product.id());
        assertNotNull(product.title());
        assertNotNull(product.price());
    }

    @Test
    void getProductById_WhenNotExists_ShouldThrowException() {
        // Given
        Long nonExistentId = 999999L;

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(nonExistentId);
        });
    }

    @Test
    void findProductById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        Long nonExistentId = 999999L;

        // When
        Optional<ProductDTO> result = productService.findProductById(nonExistentId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllCategories_ShouldReturnCategories() {
        // When
        var categories = productService.getAllCategories();

        // Then
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.size() >= 4); // FakeStore has at least 4 categories
    }
}