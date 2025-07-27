package pe.edu.elitec.ecommerce.service;

import pe.edu.elitec.ecommerce.dto.CategoryDTO;
import pe.edu.elitec.ecommerce.dto.ProductDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    Optional<ProductDTO> findProductById(Long id);
    List<CategoryDTO> getAllCategories();
    List<ProductDTO> getProductsByCategory(String category);
    List<ProductDTO> getProductsWithLimit(int limit);
    boolean categoryExists(String category);
}
