package com.flogin.service.product;

import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.entity.product.Product;
import com.flogin.repository.product.ProductRepository;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validProductRequest;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // Setup test data
        validProductRequest = new ProductRequest();
        validProductRequest.setName("Test Product");
        validProductRequest.setDescription("Test Description");
        validProductRequest.setPrice(new BigDecimal("99.99"));
        validProductRequest.setQuantity(10);

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Test Product");
        mockProduct.setPrice(new BigDecimal("99.99"));
        mockProduct.setQuantity(10);
    }

    // ============= CREATE TESTS =============

    @Test
    @DisplayName("Should create product successfully with valid data")
    void testCreateProduct_Success() {
        // Arrange
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when product name is empty")
    void testCreateProduct_EmptyName_ThrowsException() {
        // Arrange
        validProductRequest.setName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product name is required"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when product name is null")
    void testCreateProduct_NullName_ThrowsException() {
        // Arrange
        validProductRequest.setName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product name is required"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for negative price")
    void testCreateProduct_NegativePrice_ThrowsException() {
        // Arrange
        validProductRequest.setPrice(new BigDecimal("-10.00"));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product price must be greater than 0"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for zero price")
    void testCreateProduct_ZeroPrice_ThrowsException() {
        // Arrange
        validProductRequest.setPrice(BigDecimal.ZERO);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product price must be greater than 0"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for negative quantity")
    void testCreateProduct_NegativeQuantity_ThrowsException() {
        // Arrange
        validProductRequest.setQuantity(-5);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product quantity must be greater than or equal to 0"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should allow zero quantity for out-of-stock products")
    void testCreateProduct_ZeroQuantity_Success() {
        // Arrange
        validProductRequest.setQuantity(0);
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM DÒNG NÀY
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should trim whitespace from product name")
    void testCreateProduct_TrimName() {
        // Arrange
        validProductRequest.setName("  Test Product  ");
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM DÒNG NÀY
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            assertEquals("Test Product", savedProduct.getName());
            return savedProduct;
        });

        // Act
        productService.createProduct(validProductRequest);

        // Assert - verified in the answer above
    }

    @Test
    @DisplayName("Should throw BadRequestException when product name is too long")
    void testCreateProduct_LongName() {
        // Arrange
        validProductRequest.setName("A".repeat(101)); // > 100 characters

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product name must not exceed 100 characters"));
        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    // ============= READ TESTS =============

    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts_Success() {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setQuantity(5);

        List<Product> mockProductList = Arrays.asList(mockProduct, product2);  // ← SỬA: Tạo List
        when(productRepository.findAll()).thenReturn(mockProductList);  // ← SỬA: Trả về List

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProducts_EmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList());  // ← SỬA: Trả về empty list

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // Act
        ProductResponse result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when product ID not found")
    void testGetProductById_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.getProductById(999L)
        );
        
        assertTrue(exception.getMessage().contains("Product not found with id: 999"));
        verify(productRepository, times(1)).findById(999L);
    }

    // ============= UPDATE TESTS =============

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setPrice(new BigDecimal("149.99"));
        updateRequest.setQuantity(20);

        // Act
        ProductResponse result = productService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent product")
    void testUpdateProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.updateProduct(999L, validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when updating with invalid data")
    void testUpdateProduct_InvalidData_ThrowsException() {
        // Arrange
        validProductRequest.setPrice(new BigDecimal("-50.00"));
        
        // KHÔNG CẦN mock findById vì validation xảy ra TRƯỚC khi gọi findById

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.updateProduct(1L, validProductRequest)
        );
        
        assertTrue(exception.getMessage().contains("Product price must be greater than 0"));
        verify(productRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    // ============= DELETE TESTS =============

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent product")
    void testDeleteProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.deleteProduct(999L)
        );
        
        assertTrue(exception.getMessage().contains("Product not found with id: 999"));
        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    // ============= EDGE CASES =============

    @Test
    @DisplayName("Should handle very large price values")
    void testCreateProduct_LargePrice() {
        // Arrange
        validProductRequest.setPrice(new BigDecimal("999999999.99"));
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle max length product names (100 chars)")  // ← ĐỔI TÊN
    void testCreateProduct_MaxLengthName_Success() {  // ← ĐỔI TÊN METHOD
        // Arrange
        String maxName = "A".repeat(100); // Exactly 100 characters ← SỬA
        validProductRequest.setName(maxName);
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle null description")
    void testCreateProduct_NullDescription_Success() {
        // Arrange
        validProductRequest.setDescription(null);
        when(productRepository.existsByName(anyString())).thenReturn(false);  // ← THÊM
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should search products by name successfully")
    void testSearchProducts_Success() {
        // Arrange
        List<Product> searchResults = Arrays.asList(mockProduct);  // ← SỬA
        when(productRepository.findByNameContainingIgnoreCase("Test"))
            .thenReturn(searchResults);  // ← SỬA

        // Act
        List<ProductResponse> result = productService.searchProducts("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Should return empty list when search finds nothing")
    void testSearchProducts_NoResults() {
        // Arrange
        when(productRepository.findByNameContainingIgnoreCase("NonExistent"))
            .thenReturn(Arrays.asList());  // ← SỬA: Empty list

        // Act
        List<ProductResponse> result = productService.searchProducts("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("NonExistent");
    }
}