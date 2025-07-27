package pe.edu.elitec.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RatingDTO(
        @JsonProperty("rate")
        @NotNull(message = "Rate cannot be null")
        @DecimalMin(value = "0.0", message = "Rate must be at least 0.0")
        @DecimalMax(value = "5.0", message = "Rate must be at most 5.0")
        Double rate,
        @JsonProperty("count")
        @NotNull(message = "Count cannot be null")
        @PositiveOrZero(message = "Count must be positive or zero")
        Integer count
) {

    //CONSTRUCTOR COMPACTO
    public RatingDTO {
        // Validación de rate
        if (rate != null && (rate < 0.0 || rate > 5.0)) {
            throw new IllegalArgumentException("Rate must be between 0.0 and 5.0");
        }

        // Validación de count
        if (count != null && count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
    }

    public boolean hasRatings() {
        return count != null && count > 0;
    }

    public String getFormattedRating() {
        if (rate == null) return "No rating";

        return String.format("%.1f/5.0 (%d reviews)",
                rate,
                count != null ? count : 0);
    }
}