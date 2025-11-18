package com.flogin.service.product;

import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.entity.product.Product;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import com.flogin.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create new product
     */
    public ProductResponse createProduct(ProductRequest request) {
        // Validate request
        validateProductRequest(request);

        // Trim name
        String name = request.getName().trim();

        // Check if product name already exists
        if (productRepository.existsByName(name)) {
            throw new BadRequestException("Product name already exists");
        }

        // Create product
        Product product = new Product();
        product.setName(name);
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        // Save and return
        Product savedProduct = productRepository.save(product);
        return new ProductResponse(savedProduct);
    }

    /**
     * Get product by ID
     */
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        return new ProductResponse(product);
    }

    /**
     * Get all products
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(ProductResponse::new)
            .collect(Collectors.toList());
    }

    /**
     * Update product
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // Validate request
        validateProductRequest(request);

        // Find existing product
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        // Trim name
        String name = request.getName().trim();

        // Check if new name already exists (excluding current product)
        if (!product.getName().equals(name) && productRepository.existsByName(name)) {
            throw new BadRequestException("Product name already exists");
        }

        // Update fields
        product.setName(name);
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        // Save and return
        Product updatedProduct = productRepository.save(product);
        return new ProductResponse(updatedProduct);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Search products by name
     */
    public List<ProductResponse> searchProducts(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Search name is required");
        }

        return productRepository.findByNameContainingIgnoreCase(name.trim()).stream()
            .map(ProductResponse::new)
            .collect(Collectors.toList());
    }

    /**
     * Validate product request
     */
    private void validateProductRequest(ProductRequest request) {
        // Check null
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Product name is required");
        }
        if (request.getPrice() == null) {
            throw new BadRequestException("Product price is required");
        }
        if (request.getQuantity() == null) {
            throw new BadRequestException("Product quantity is required");
        }

        // Validate price
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than 0");
        }

        // Validate quantity
        if (request.getQuantity() < 0) {
            throw new BadRequestException("Product quantity must be greater than or equal to 0");
        }

        // Validate name length
        if (request.getName().trim().length() < 3) {
            throw new BadRequestException("Product name must be at least 3 characters");
        }
        if (request.getName().trim().length() > 100) {
            throw new BadRequestException("Product name must not exceed 100 characters");
        }
    }
}