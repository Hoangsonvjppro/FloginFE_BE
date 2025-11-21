package com.flogin.dto.product;

import com.flogin.entity.product.Category;
import com.flogin.entity.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    
    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            categoryResponse = new CategoryResponse();
            categoryResponse.setId(category.getId());
            categoryResponse.setName(category.getName());
            categoryResponse.setDescription(category.getDescription());
            categoryResponse.setCreatedAt(category.getCreatedAt());
            categoryResponse.setUpdatedAt(category.getUpdatedAt());
        }
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(categoryResponse)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    public Product toEntity(ProductRequest request, Category category) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);
        
        return product;
    }
    
    public void updateEntity(Product product, ProductRequest request, Category category) {
        if (product == null || request == null) {
            return;
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);
    }
}
