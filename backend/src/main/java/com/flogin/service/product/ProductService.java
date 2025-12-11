package com.flogin.service.product;

import com.flogin.dto.product.ProductMapper;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.entity.product.Category;
import com.flogin.entity.product.Product;
import com.flogin.repository.product.ProductRepository;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service
 * 
 * Validation Rules theo Assignment:
 * - Name: 3-100 ký tự, không được rỗng
 * - Price: > 0 và <= 999,999,999
 * - Quantity: >= 0 và <= 99,999
 * - Description: <= 500 ký tự (optional)
 * - Category: Phải thuộc danh sách categories có sẵn
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999");
    private static final int MAX_QUANTITY = 99999;
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Validate request
        validateProductRequest(request);
        
        // Trim name and description
        request.setName(request.getName().trim());
        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
        }
        
        // Create product
        Product product = productMapper.toEntity(request);
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
        
        // Update product
        productMapper.updateEntity(product, request);
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
    
    /**
     * Validate product request theo assignment rules
     */
    private void validateProductRequest(ProductRequest request) {
        // Validate Name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Product name is required");
        }
        
        String trimmedName = request.getName().trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new BadRequestException("Product name must be at least " + MIN_NAME_LENGTH + " characters");
        }
        
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("Product name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        
        // Validate Price
        if (request.getPrice() == null) {
            throw new BadRequestException("Price is required");
        }
        
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than 0");
        }
        
        if (request.getPrice().compareTo(MAX_PRICE) > 0) {
            throw new BadRequestException("Price must not exceed 999,999,999");
        }
        
        // Validate Quantity
        if (request.getQuantity() == null) {
            throw new BadRequestException("Quantity is required");
        }
        
        if (request.getQuantity() < 0) {
            throw new BadRequestException("Quantity must be greater than or equal to 0");
        }
        
        if (request.getQuantity() > MAX_QUANTITY) {
            throw new BadRequestException("Quantity must not exceed " + MAX_QUANTITY);
        }
        
        // Validate Description (optional)
        if (request.getDescription() != null && request.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        
        // Validate Category
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Category is required");
        }
        
        if (!Category.isValid(request.getCategory())) {
            throw new BadRequestException("Invalid category: " + request.getCategory() + 
                ". Valid categories are: ELECTRONICS, CLOTHING, FOOD, BOOKS, SPORTS, HOME, OTHER");
        }
    }
}
