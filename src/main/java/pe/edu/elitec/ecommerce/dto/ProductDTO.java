package pe.edu.elitec.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

public record ProductDTO(
        @JsonProperty("id")
        @Positive(message = "Product ID must be positive")
        Long id,

        @JsonProperty("title")
        @NotBlank(message = "Product title is required")
        @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
        String title,

        @JsonProperty("price")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
        BigDecimal price,

        @JsonProperty("description")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @JsonProperty("category")
        @NotBlank(message = "Category is required")
        @Pattern(regexp = "^[a-zA-Z0-9\\s'&-]+$", message = "Category contains invalid characters")
        String category,

        @JsonProperty("image")
        @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp).*$",
                message = "Image must be a valid URL with image extension")
        String image,

        @JsonProperty("rating")
        @Valid
        RatingDTO rating
) {
    // Constructor compacto para validaciones de negocio
    public ProductDTO {
        if (title != null) {
            title = title.trim();
        }
        if (category != null) {
            category = category.trim().toLowerCase();
        }
    }

    //Método de conveniencia para verificar si tiene imagen
    public boolean hasImage() {
        return image != null && !image.trim().isEmpty();
    }

    //Método para obtener precio formateado
    public String getFormattedPrice() {
        if (price == null) return "$0.00";
        return String.format("$%.2f", price);
    }

    //Método para verificar si es producto premium (precio > $100)
    public boolean isPremium() {
        return price != null && price.compareTo(BigDecimal.valueOf(100)) > 0;
    }

    //Método para obtener resumen del producto
    public String getSummary() {
        return String.format("%s - %s (%s)",
                title != null ? title : "Unknown",
                getFormattedPrice(),
                category != null ? category : "uncategorized");
    }

    //Factory method para crear producto con datos mínimos
    public static ProductDTO createMinimal(Long id, String title, BigDecimal price, String category) {
        return new ProductDTO(id, title, price, null, category, null, null);
    }
}
