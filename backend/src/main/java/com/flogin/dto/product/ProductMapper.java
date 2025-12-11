package com.flogin.dto.product;

import com.flogin.entity.product.Category;
import com.flogin.entity.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory() != null ? product.getCategory().name() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    public Product toEntity(ProductRequest request) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(Category.fromString(request.getCategory()));
        
        return product;
    }
    
    public void updateEntity(Product product, ProductRequest request) {
        if (product == null || request == null) {
            return;
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(Category.fromString(request.getCategory()));
    }
}
