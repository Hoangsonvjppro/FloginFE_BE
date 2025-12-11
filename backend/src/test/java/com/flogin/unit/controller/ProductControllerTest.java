package com.flogin.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.controller.ProductController;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import com.flogin.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests for ProductController
 * 
 * Tests REST API endpoints using @WebMvcTest
 * - GET /api/products - Get all products
 * - GET /api/products/{id} - Get product by ID
 * - POST /api/products - Create product
 * - PUT /api/products/{id} - Update product
 * - DELETE /api/products/{id} - Delete product
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ProductRequest validRequest;
    private ProductResponse sampleResponse;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        validRequest = new ProductRequest();
        validRequest.setName("Test Product");
        validRequest.setDescription("Test description");
        validRequest.setPrice(new BigDecimal("99.99"));
        validRequest.setQuantity(100);
        validRequest.setCategory("ELECTRONICS");
        
        sampleResponse = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .description("Test description")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .category("ELECTRONICS")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    // ==================== GET ALL PRODUCTS ====================
    @Nested
    @DisplayName("GET /api/products - Get All Products")
    class GetAllProductsTests {
        
        @Test
        @DisplayName("Should return empty list when no products exist")
        void getAllProducts_Empty_ReturnsEmptyList() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.emptyList());
            
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
            
            verify(productService).getAllProducts();
        }
        
        @Test
        @DisplayName("Should return list of products")
        void getAllProducts_WithProducts_ReturnsList() throws Exception {
            ProductResponse product2 = ProductResponse.builder()
                    .id(2L)
                    .name("Product 2")
                    .description("Description 2")
                    .price(new BigDecimal("49.99"))
                    .quantity(50)
                    .category("CLOTHING")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            
            List<ProductResponse> products = Arrays.asList(sampleResponse, product2);
            when(productService.getAllProducts()).thenReturn(products);
            
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Product"))
                    .andExpect(jsonPath("$[0].category").value("ELECTRONICS"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Product 2"))
                    .andExpect(jsonPath("$[1].category").value("CLOTHING"));
            
            verify(productService).getAllProducts();
        }
        
        @Test
        @DisplayName("Should return all product fields correctly")
        void getAllProducts_VerifyAllFields() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleResponse));
            
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Product"))
                    .andExpect(jsonPath("$[0].description").value("Test description"))
                    .andExpect(jsonPath("$[0].price").value(99.99))
                    .andExpect(jsonPath("$[0].quantity").value(100))
                    .andExpect(jsonPath("$[0].category").value("ELECTRONICS"))
                    .andExpect(jsonPath("$[0].createdAt").exists())
                    .andExpect(jsonPath("$[0].updatedAt").exists());
        }
    }
    
    // ==================== GET PRODUCT BY ID ====================
    @Nested
    @DisplayName("GET /api/products/{id} - Get Product by ID")
    class GetProductByIdTests {
        
        @Test
        @DisplayName("Should return product when exists")
        void getProductById_Exists_ReturnsProduct() throws Exception {
            when(productService.getProductById(1L)).thenReturn(sampleResponse);
            
            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.category").value("ELECTRONICS"));
            
            verify(productService).getProductById(1L);
        }
        
        @Test
        @DisplayName("Should return 404 when product not found")
        void getProductById_NotFound_Returns404() throws Exception {
            when(productService.getProductById(999L))
                    .thenThrow(new NotFoundException("Product not found with id: 999"));
            
            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound());
            
            verify(productService).getProductById(999L);
        }
    }
    
    // ==================== CREATE PRODUCT ====================
    @Nested
    @DisplayName("POST /api/products - Create Product")
    class CreateProductTests {
        
        @Test
        @DisplayName("Should create product with valid data - returns 201")
        void createProduct_ValidData_Returns201() throws Exception {
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.category").value("ELECTRONICS"));
            
            verify(productService).createProduct(any(ProductRequest.class));
        }
        
        @Test
        @DisplayName("Should return 400 when name is blank")
        void createProduct_BlankName_Returns400() throws Exception {
            validRequest.setName("");
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when name is too short")
        void createProduct_NameTooShort_Returns400() throws Exception {
            validRequest.setName("ab"); // 2 chars, min is 3
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when name exceeds 100 characters")
        void createProduct_NameTooLong_Returns400() throws Exception {
            validRequest.setName("a".repeat(101)); // 101 chars, max is 100
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when price is null")
        void createProduct_NullPrice_Returns400() throws Exception {
            validRequest.setPrice(null);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when price is zero")
        void createProduct_ZeroPrice_Returns400() throws Exception {
            validRequest.setPrice(BigDecimal.ZERO);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when price exceeds maximum")
        void createProduct_PriceExceedsMax_Returns400() throws Exception {
            validRequest.setPrice(new BigDecimal("1000000000")); // > 999,999,999
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when quantity is null")
        void createProduct_NullQuantity_Returns400() throws Exception {
            validRequest.setQuantity(null);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when quantity is negative")
        void createProduct_NegativeQuantity_Returns400() throws Exception {
            validRequest.setQuantity(-1);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when quantity exceeds maximum")
        void createProduct_QuantityExceedsMax_Returns400() throws Exception {
            validRequest.setQuantity(100000); // > 99,999
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when category is blank")
        void createProduct_BlankCategory_Returns400() throws Exception {
            validRequest.setCategory("");
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when description exceeds 500 characters")
        void createProduct_DescriptionTooLong_Returns400() throws Exception {
            validRequest.setDescription("a".repeat(501)); // 501 chars, max is 500
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when category is invalid")
        void createProduct_InvalidCategory_Returns400() throws Exception {
            validRequest.setCategory("INVALID_CATEGORY");
            
            when(productService.createProduct(any(ProductRequest.class)))
                    .thenThrow(new BadRequestException("Invalid category: INVALID_CATEGORY"));
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should accept minimum valid values")
        void createProduct_MinimumValidValues_Returns201() throws Exception {
            validRequest.setName("abc"); // exactly 3 chars
            validRequest.setPrice(new BigDecimal("0.01")); // minimum price
            validRequest.setQuantity(0); // minimum quantity
            validRequest.setDescription(null); // optional
            
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept maximum valid values")
        void createProduct_MaximumValidValues_Returns201() throws Exception {
            validRequest.setName("a".repeat(100)); // exactly 100 chars
            validRequest.setPrice(new BigDecimal("999999999")); // maximum price
            validRequest.setQuantity(99999); // maximum quantity
            validRequest.setDescription("a".repeat(500)); // exactly 500 chars
            
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }
    
    // ==================== UPDATE PRODUCT ====================
    @Nested
    @DisplayName("PUT /api/products/{id} - Update Product")
    class UpdateProductTests {
        
        @Test
        @DisplayName("Should update product with valid data - returns 200")
        void updateProduct_ValidData_Returns200() throws Exception {
            ProductResponse updatedResponse = ProductResponse.builder()
                    .id(1L)
                    .name("Updated Product")
                    .description("Updated description")
                    .price(new BigDecimal("149.99"))
                    .quantity(200)
                    .category("CLOTHING")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            
            validRequest.setName("Updated Product");
            validRequest.setCategory("CLOTHING");
            
            when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                    .thenReturn(updatedResponse);
            
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated Product"))
                    .andExpect(jsonPath("$.category").value("CLOTHING"));
            
            verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
        }
        
        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void updateProduct_NotFound_Returns404() throws Exception {
            when(productService.updateProduct(eq(999L), any(ProductRequest.class)))
                    .thenThrow(new NotFoundException("Product not found with id: 999"));
            
            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("Should return 400 when update data is invalid")
        void updateProduct_InvalidData_Returns400() throws Exception {
            validRequest.setName(""); // invalid
            
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when updating with invalid category")
        void updateProduct_InvalidCategory_Returns400() throws Exception {
            validRequest.setCategory("INVALID");
            
            when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                    .thenThrow(new BadRequestException("Invalid category"));
            
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
    
    // ==================== DELETE PRODUCT ====================
    @Nested
    @DisplayName("DELETE /api/products/{id} - Delete Product")
    class DeleteProductTests {
        
        @Test
        @DisplayName("Should delete product - returns 204 No Content")
        void deleteProduct_Exists_Returns204() throws Exception {
            doNothing().when(productService).deleteProduct(1L);
            
            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());
            
            verify(productService).deleteProduct(1L);
        }
        
        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void deleteProduct_NotFound_Returns404() throws Exception {
            doThrow(new NotFoundException("Product not found with id: 999"))
                    .when(productService).deleteProduct(999L);
            
            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound());
            
            verify(productService).deleteProduct(999L);
        }
    }
    
    // ==================== CATEGORY VALIDATION ====================
    @Nested
    @DisplayName("Category Validation Tests")
    class CategoryValidationTests {
        
        @Test
        @DisplayName("Should accept ELECTRONICS category")
        void createProduct_ElectronicsCategory_Success() throws Exception {
            validRequest.setCategory("ELECTRONICS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept CLOTHING category")
        void createProduct_ClothingCategory_Success() throws Exception {
            validRequest.setCategory("CLOTHING");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept FOOD category")
        void createProduct_FoodCategory_Success() throws Exception {
            validRequest.setCategory("FOOD");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept BOOKS category")
        void createProduct_BooksCategory_Success() throws Exception {
            validRequest.setCategory("BOOKS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept SPORTS category")
        void createProduct_SportsCategory_Success() throws Exception {
            validRequest.setCategory("SPORTS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept HOME category")
        void createProduct_HomeCategory_Success() throws Exception {
            validRequest.setCategory("HOME");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should accept OTHER category")
        void createProduct_OtherCategory_Success() throws Exception {
            validRequest.setCategory("OTHER");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
    }
}
