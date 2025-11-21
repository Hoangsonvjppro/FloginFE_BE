package com.flogin.service.product;

import com.flogin.dto.product.ProductMapper;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.entity.product.Category;
import com.flogin.entity.product.Product;
import com.flogin.repository.product.CategoryRepository;
import com.flogin.repository.product.ProductRepository;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Validate request
        validateProductRequest(request);
        
        // Trim name and description
        request.setName(request.getName().trim());
        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
        }
        
        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + request.getCategoryId()));
        
        // Create product
        Product product = productMapper.toEntity(request, category);
        Product savedProduct = productRepository.save(product);
        
        return productMapper.toResponse(savedProduct);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        
        return productMapper.toResponse(product);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // Find existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        
        // Validate request
        validateProductRequest(request);
        
        // Trim name and description
        request.setName(request.getName().trim());
        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
        }
        
        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + request.getCategoryId()));
        
        // Update product
        productMapper.updateEntity(product, request, category);
        Product updatedProduct = productRepository.save(product);
        
        return productMapper.toResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        
        productRepository.deleteById(id);
    }
    
    private void validateProductRequest(ProductRequest request) {
        // Name validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Product name is required");
        }
        if (request.getName().trim().length() < 3 || request.getName().trim().length() > 100) {
            throw new BadRequestException("Product name must be between 3 and 100 characters");
        }
        
        // Description validation
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            throw new BadRequestException("Description must not exceed 500 characters");
        }
        
        // Price validation
        if (request.getPrice() == null) {
            throw new BadRequestException("Price is required");
        }
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than 0");
        }
        if (request.getPrice().compareTo(new BigDecimal("999999999")) > 0) {
            throw new BadRequestException("Price must not exceed 999,999,999");
        }
        
        // Quantity validation
        if (request.getQuantity() == null) {
            throw new BadRequestException("Quantity is required");
        }
        if (request.getQuantity() < 0) {
            throw new BadRequestException("Quantity must be greater than or equal to 0");
        }
        if (request.getQuantity() > 99999) {
            throw new BadRequestException("Quantity must not exceed 99,999");
        }
        
        // Category validation
        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category is required");
        }
    }
}
