package pe.edu.elitec.ecommerce.exception;

//Exception lanzada cuando un producto no se encuentra
public class ProductNotFoundException extends RuntimeException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super(String.format("Product with ID %d not found", productId));
        this.productId = productId;
    }

    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
    }

    public Long getProductId() {
        return productId;
    }
}