package pe.edu.elitec.ecommerce.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductDTOTest {

    @Test
    void testProductDTOCreation(){

        RatingDTO rating = new RatingDTO(4.5, 120);

        ProductDTO product = new ProductDTO(
                1L,
                "Test Product",
                new BigDecimal("99.99"),
                "Test description",
                "electronics",
                "https://example.com/image.jpg",
                rating
        );

        assertNotNull(product);
        assertEquals(1L, product.id());
        assertEquals("Test Product", product.title());
        assertEquals(new BigDecimal("99.99"), product.price());
        assertEquals("electronics", product.category());
        assertFalse(product.isPremium()); // < $100
        assertEquals("$99.99", product.getFormattedPrice());
    }

    @Test
    void testRatingDTOValidation() {
        assertDoesNotThrow(() -> new RatingDTO(4.5, 120));
        assertThrows(IllegalArgumentException.class, () -> new RatingDTO(6.0, 120)); // > 5.0
        assertThrows(IllegalArgumentException.class, () -> new RatingDTO(4.5, -1)); // negative count
    }
}