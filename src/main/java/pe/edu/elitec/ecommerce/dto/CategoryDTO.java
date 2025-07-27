package pe.edu.elitec.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryDTO(
        @NotBlank(message = "Category name is required")
        @Pattern(regexp = "^[a-zA-Z0-9\\s'&-]+$", message = "Category name contains invalid characters")
        String name
) {
    //Constructor compacto para normalización
    public CategoryDTO {
        if (name != null) {
            name = name.trim().toLowerCase();
        }
    }

    //Método para obtener nombre capitalizado
    public String getDisplayName() {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    //Factory method desde String
    public static CategoryDTO from(String categoryName) {
        return new CategoryDTO(categoryName);
    }
}
