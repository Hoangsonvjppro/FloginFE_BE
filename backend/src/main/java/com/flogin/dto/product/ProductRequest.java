package com.flogin.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Product Request DTO
 * 
 * Validation Rules theo Assignment:
 * - Name: 3-100 ký tự, không được rỗng
 * - Price: > 0 và <= 999,999,999
 * - Quantity: >= 0 và <= 99,999
 * - Description: <= 500 ký tự (optional)
 * - Category: Phải thuộc danh sách categories có sẵn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999999", message = "Price must not exceed 999,999,999")
    private BigDecimal price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    @Max(value = 99999, message = "Quantity must not exceed 99,999")
    private Integer quantity;
    
    @NotBlank(message = "Category is required")
    private String category;
}
