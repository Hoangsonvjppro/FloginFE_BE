package com.flogin.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flogin.controller.ProductController;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests cho Product API
 * 
 * Sử dụng @WebMvcTest với @AutoConfigureMockMvc
 * Tests full request/response cycle qua HTTP endpoints
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Product API Integration Tests")
class ProductIntegrationTest {
    
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
    @DisplayName("GET /api/products Tests")
    class GetAllProductsTests {
        
        @Test
        @DisplayName("IT_PROD_GET_001: Get all products - Empty list")
        void getAllProducts_EmptyList_Returns200() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.emptyList());
            
            mockMvc.perform(get("/api/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
        
        @Test
        @DisplayName("IT_PROD_GET_002: Get all products - Multiple products")
        void getAllProducts_WithProducts_Returns200() throws Exception {
            ProductResponse product2 = ProductResponse.builder()
                    .id(2L)
                    .name("Product 2")
                    .price(new BigDecimal("49.99"))
                    .quantity(50)
                    .category("CLOTHING")
                    .build();
            
            List<ProductResponse> products = Arrays.asList(sampleResponse, product2);
            when(productService.getAllProducts()).thenReturn(products);
            
            mockMvc.perform(get("/api/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Product"))
                    .andExpect(jsonPath("$[0].category").value("ELECTRONICS"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].category").value("CLOTHING"));
        }
        
        @Test
        @DisplayName("IT_PROD_GET_003: Get all products - Verify all fields")
        void getAllProducts_VerifyAllFields() throws Exception {
            when(productService.getAllProducts()).thenReturn(Collections.singletonList(sampleResponse));
            
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Product"))
                    .andExpect(jsonPath("$[0].description").value("Test description"))
                    .andExpect(jsonPath("$[0].price").value(99.99))
                    .andExpect(jsonPath("$[0].quantity").value(100))
                    .andExpect(jsonPath("$[0].category").value("ELECTRONICS"));
        }
    }
    
    // ==================== GET PRODUCT BY ID ====================
    @Nested
    @DisplayName("GET /api/products/{id} Tests")
    class GetProductByIdTests {
        
        @Test
        @DisplayName("IT_PROD_GETID_001: Get existing product")
        void getProductById_Exists_Returns200() throws Exception {
            when(productService.getProductById(1L)).thenReturn(sampleResponse);
            
            mockMvc.perform(get("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.category").value("ELECTRONICS"));
        }
    }
    
    // ==================== CREATE PRODUCT ====================
    @Nested
    @DisplayName("POST /api/products Tests")
    class CreateProductTests {
        
        @Test
        @DisplayName("IT_PROD_CREATE_001: Create with valid data")
        void createProduct_ValidData_Returns201() throws Exception {
            when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.category").value("ELECTRONICS"));
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_002: Create with minimum valid values")
        void createProduct_MinimumValues_Returns201() throws Exception {
            validRequest.setName("abc"); // 3 chars min
            validRequest.setPrice(new BigDecimal("0.01")); // min price
            validRequest.setQuantity(0); // min quantity
            validRequest.setDescription(null);
            
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_003: Create with maximum valid values")
        void createProduct_MaximumValues_Returns201() throws Exception {
            validRequest.setName("a".repeat(100)); // 100 chars max
            validRequest.setPrice(new BigDecimal("999999999")); // max price
            validRequest.setQuantity(99999); // max quantity
            validRequest.setDescription("a".repeat(500)); // 500 chars max
            
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_004: Blank name -> 400")
        void createProduct_BlankName_Returns400() throws Exception {
            validRequest.setName("");
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_005: Name too short -> 400")
        void createProduct_NameTooShort_Returns400() throws Exception {
            validRequest.setName("ab"); // < 3 chars
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_006: Name too long -> 400")
        void createProduct_NameTooLong_Returns400() throws Exception {
            validRequest.setName("a".repeat(101)); // > 100 chars
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_007: Price is zero -> 400")
        void createProduct_ZeroPrice_Returns400() throws Exception {
            validRequest.setPrice(BigDecimal.ZERO);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_008: Price exceeds max -> 400")
        void createProduct_PriceExceedsMax_Returns400() throws Exception {
            validRequest.setPrice(new BigDecimal("1000000000")); // > 999,999,999
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_009: Negative quantity -> 400")
        void createProduct_NegativeQuantity_Returns400() throws Exception {
            validRequest.setQuantity(-1);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_010: Quantity exceeds max -> 400")
        void createProduct_QuantityExceedsMax_Returns400() throws Exception {
            validRequest.setQuantity(100000); // > 99,999
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_011: Description too long -> 400")
        void createProduct_DescriptionTooLong_Returns400() throws Exception {
            validRequest.setDescription("a".repeat(501)); // > 500 chars
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("IT_PROD_CREATE_012: Blank category -> 400")
        void createProduct_BlankCategory_Returns400() throws Exception {
            validRequest.setCategory("");
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
    
    // ==================== UPDATE PRODUCT ====================
    @Nested
    @DisplayName("PUT /api/products/{id} Tests")
    class UpdateProductTests {
        
        @Test
        @DisplayName("IT_PROD_UPDATE_001: Update with valid data")
        void updateProduct_ValidData_Returns200() throws Exception {
            ProductResponse updatedResponse = ProductResponse.builder()
                    .id(1L)
                    .name("Updated Product")
                    .price(new BigDecimal("149.99"))
                    .quantity(200)
                    .category("CLOTHING")
                    .build();
            
            validRequest.setName("Updated Product");
            validRequest.setCategory("CLOTHING");
            
            when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
                    .thenReturn(updatedResponse);
            
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Product"))
                    .andExpect(jsonPath("$.category").value("CLOTHING"));
        }
        
        @Test
        @DisplayName("IT_PROD_UPDATE_002: Update with invalid data -> 400")
        void updateProduct_InvalidData_Returns400() throws Exception {
            validRequest.setName(""); // invalid
            
            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
    
    // ==================== DELETE PRODUCT ====================
    @Nested
    @DisplayName("DELETE /api/products/{id} Tests")
    class DeleteProductTests {
        
        @Test
        @DisplayName("IT_PROD_DELETE_001: Delete existing product")
        void deleteProduct_Exists_Returns204() throws Exception {
            doNothing().when(productService).deleteProduct(1L);
            
            mockMvc.perform(delete("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            
            verify(productService).deleteProduct(1L);
        }
    }
    
    // ==================== CATEGORY TESTS ====================
    @Nested
    @DisplayName("Category Validation Tests")
    class CategoryTests {
        
        @Test
        @DisplayName("IT_PROD_CAT_001: ELECTRONICS category")
        void createProduct_ElectronicsCategory_Success() throws Exception {
            validRequest.setCategory("ELECTRONICS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_002: CLOTHING category")
        void createProduct_ClothingCategory_Success() throws Exception {
            validRequest.setCategory("CLOTHING");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_003: FOOD category")
        void createProduct_FoodCategory_Success() throws Exception {
            validRequest.setCategory("FOOD");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_004: BOOKS category")
        void createProduct_BooksCategory_Success() throws Exception {
            validRequest.setCategory("BOOKS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_005: SPORTS category")
        void createProduct_SportsCategory_Success() throws Exception {
            validRequest.setCategory("SPORTS");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_006: HOME category")
        void createProduct_HomeCategory_Success() throws Exception {
            validRequest.setCategory("HOME");
            when(productService.createProduct(any())).thenReturn(sampleResponse);
            
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("IT_PROD_CAT_007: OTHER category")
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
