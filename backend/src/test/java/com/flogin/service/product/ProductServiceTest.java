package com.flogin.service.product;

import com.flogin.dto.product.ProductMapper;
import com.flogin.dto.product.ProductRequest;
import com.flogin.dto.product.ProductResponse;
import com.flogin.entity.product.Product;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import com.flogin.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho ProductService sử dụng JUnit 5 và Mockito.
 * 
 * Coverage:
 * - createProduct: Happy Path, Validation errors (price, quantity, name)
 * - getAllProducts: Danh sách rỗng, Danh sách có sản phẩm
 * - searchProducts: Tìm thấy, không tìm thấy
 * - getProductById: Tìm thấy, không tìm thấy (NotFoundException)
 * - updateProduct: Success, Not Found
 * - deleteProduct: Success, Not Found
 * 
 * Target Coverage: >= 85%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest validProductRequest;
    private ProductResponse testProductResponse;

    @BeforeEach
    void setUp() {
        // Setup test product entity
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setQuantity(100);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        // Setup valid product request
        validProductRequest = new ProductRequest();
        validProductRequest.setName("New Product");
        validProductRequest.setDescription("New Description");
        validProductRequest.setPrice(new BigDecimal("149.99"));
        validProductRequest.setQuantity(50);

        // Setup product response
        testProductResponse = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    @DisplayName("Create Product - Success: Input hợp lệ -> Gọi save -> Trả về ProductResponse")
    void createProduct_WithValidInput_ShouldSaveAndReturnResponse() {
        // Arrange
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(validProductRequest);

        // Assert
        assertNotNull(result, "ProductResponse không được null");
        assertEquals(testProductResponse.getId(), result.getId());
        assertEquals(testProductResponse.getName(), result.getName());
        assertEquals(testProductResponse.getPrice(), result.getPrice());

        // Verify interactions
        verify(productMapper, times(1)).toEntity(any(ProductRequest.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Name rỗng -> Ném BadRequestException")
    void createProduct_WithEmptyName_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi name rỗng"
        );

        assertEquals("Product name is required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Name null -> Ném BadRequestException")
    void createProduct_WithNullName_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName(null);
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi name null"
        );

        assertEquals("Product name is required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Name chỉ chứa khoảng trắng -> Ném BadRequestException")
    void createProduct_WithWhitespaceOnlyName_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("   ");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi name chỉ chứa khoảng trắng"
        );

        assertEquals("Product name is required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Price null -> Ném BadRequestException")
    void createProduct_WithNullPrice_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(null);
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi price null"
        );

        assertEquals("Price is required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Price = 0 -> Ném BadRequestException")
    void createProduct_WithZeroPrice_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(BigDecimal.ZERO);
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi price = 0"
        );

        assertEquals("Price must be greater than 0", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Price < 0 -> Ném BadRequestException")
    void createProduct_WithNegativePrice_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("-10.00"));
        request.setQuantity(10);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi price < 0"
        );

        assertEquals("Price must be greater than 0", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation: Price = 0.01 (giá trị nhỏ nhất hợp lệ) -> Thành công")
    void createProduct_WithMinimumValidPrice_ShouldSucceed() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("0.01"));
        request.setQuantity(10);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Quantity null -> Ném BadRequestException")
    void createProduct_WithNullQuantity_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi quantity null"
        );

        assertEquals("Quantity is required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation Error: Quantity < 0 -> Ném BadRequestException")
    void createProduct_WithNegativeQuantity_ShouldThrowBadRequestException() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(-1);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.createProduct(request),
            "Phải throw BadRequestException khi quantity < 0"
        );

        assertEquals("Quantity must be greater than or equal to 0", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Validation: Quantity = 0 -> Thành công")
    void createProduct_WithZeroQuantity_ShouldSucceed() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Name");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(0);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Create Product - Name trimming: Khoảng trắng xung quanh name được trim")
    void createProduct_WithWhitespaceAroundName_ShouldTrimName() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("  Product Name  ");
        request.setDescription("Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(10);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        // Verify name was trimmed
        assertEquals("Product Name", request.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    @DisplayName("Get All Products - Success: Trả về danh sách rỗng khi không có sản phẩm")
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Danh sách phải rỗng khi không có sản phẩm");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get All Products - Success: Trả về danh sách ProductResponse khi có sản phẩm")
    void getAllProducts_WithProducts_ShouldReturnProductList() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("99.99"));
        product1.setQuantity(10);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("149.99"));
        product2.setQuantity(20);

        ProductResponse response1 = ProductResponse.builder()
                .id(1L)
                .name("Product 1")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        ProductResponse response2 = ProductResponse.builder()
                .id(2L)
                .name("Product 2")
                .price(new BigDecimal("149.99"))
                .quantity(20)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
        when(productMapper.toResponse(product1)).thenReturn(response1);
        when(productMapper.toResponse(product2)).thenReturn(response2);

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Danh sách phải có 2 sản phẩm");
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(2)).toResponse(any(Product.class));
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("Search Products - Success: Tìm thấy sản phẩm theo keyword")
    void searchProducts_WithMatchingKeyword_ShouldReturnMatchingProducts() {
        // Arrange
        String keyword = "laptop";
        Product product = new Product();
        product.setId(1L);
        product.setName("Gaming Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setQuantity(5);

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name("Gaming Laptop")
                .price(new BigDecimal("999.99"))
                .quantity(5)
                .build();

        when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(Arrays.asList(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gaming Laptop", result.get(0).getName());

        verify(productRepository, times(1)).findByNameContainingIgnoreCase(keyword);
        verify(productMapper, times(1)).toResponse(product);
    }

    @Test
    @DisplayName("Search Products - Success: Không tìm thấy sản phẩm -> Trả về danh sách rỗng")
    void searchProducts_WithNoMatches_ShouldReturnEmptyList() {
        // Arrange
        String keyword = "nonexistent";
        when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(Arrays.asList());

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase(keyword);
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("Get Product By ID - Found: Tìm thấy sản phẩm -> Trả về ProductResponse")
    void getProductById_WhenProductExists_ShouldReturnProductResponse() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.getProductById(productId);

        // Assert
        assertNotNull(result, "ProductResponse không được null");
        assertEquals(testProductResponse.getId(), result.getId());
        assertEquals(testProductResponse.getName(), result.getName());
        assertEquals(testProductResponse.getPrice(), result.getPrice());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).toResponse(testProduct);
    }

    @Test
    @DisplayName("Get Product By ID - Not Found: Không tìm thấy -> Ném NotFoundException")
    void getProductById_WhenProductNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.getProductById(productId),
            "Phải throw NotFoundException khi không tìm thấy product"
        );

        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    @DisplayName("Update Product - Success: Tìm thấy ID -> Update fields -> Save")
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnResponse() {
        // Arrange
        Long productId = 1L;
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setQuantity(75);

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("199.99"));
        updatedProduct.setQuantity(75);

        ProductResponse updatedResponse = ProductResponse.builder()
                .id(productId)
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("199.99"))
                .quantity(75)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productMapper).updateEntity(any(Product.class), any(ProductRequest.class));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(updatedResponse);

        // Act
        ProductResponse result = productService.updateProduct(productId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse.getName(), result.getName());
        assertEquals(updatedResponse.getPrice(), result.getPrice());
        assertEquals(updatedResponse.getQuantity(), result.getQuantity());

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntity(any(Product.class), any(ProductRequest.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Not Found: ID không tồn tại -> Ném NotFoundException")
    void updateProduct_WhenProductNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long productId = 999L;
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setQuantity(75);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.updateProduct(productId, updateRequest),
            "Phải throw NotFoundException khi không tìm thấy product để update"
        );

        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Validation: Name rỗng -> Ném BadRequestException")
    void updateProduct_WithEmptyName_ShouldThrowBadRequestException() {
        // Arrange
        Long productId = 1L;
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setQuantity(75);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.updateProduct(productId, updateRequest),
            "Phải throw BadRequestException khi update với name rỗng"
        );

        assertEquals("Product name is required", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Validation: Price <= 0 -> Ném BadRequestException")
    void updateProduct_WithInvalidPrice_ShouldThrowBadRequestException() {
        // Arrange
        Long productId = 1L;
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(BigDecimal.ZERO);
        updateRequest.setQuantity(75);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.updateProduct(productId, updateRequest),
            "Phải throw BadRequestException khi update với price = 0"
        );

        assertEquals("Price must be greater than 0", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Validation: Quantity < 0 -> Ném BadRequestException")
    void updateProduct_WithNegativeQuantity_ShouldThrowBadRequestException() {
        // Arrange
        Long productId = 1L;
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("199.99"));
        updateRequest.setQuantity(-5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productService.updateProduct(productId, updateRequest),
            "Phải throw BadRequestException khi update với quantity < 0"
        );

        assertEquals("Quantity must be greater than or equal to 0", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    @DisplayName("Delete Product - Success: ID tồn tại -> Gọi deleteById")
    void deleteProduct_WhenProductExists_ShouldDeleteSuccessfully() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Delete Product - Not Found: ID không tồn tại -> Ném NotFoundException")
    void deleteProduct_WhenProductNotExists_ShouldThrowNotFoundException() {
        // Arrange
        Long productId = 999L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productService.deleteProduct(productId),
            "Phải throw NotFoundException khi xóa product không tồn tại"
        );

        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(anyLong());
    }

    // ==================== INTEGRATION-LIKE TESTS ====================

    @Test
    @DisplayName("Create Product - Complete Workflow: Từ request đến save database")
    void createProduct_CompleteWorkflow_ShouldExecuteAllSteps() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("  Complete Product  "); // Có whitespace để test trim
        request.setDescription("Complete Description");
        request.setPrice(new BigDecimal("299.99"));
        request.setQuantity(100);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        // Verify name was trimmed
        assertEquals("Complete Product", request.getName());

        // Verify tất cả các bước được thực hiện đúng thứ tự
        verify(productMapper, times(1)).toEntity(any(ProductRequest.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Complete Workflow: Tìm -> Validate -> Update -> Save")
    void updateProduct_CompleteWorkflow_ShouldExecuteAllSteps() {
        // Arrange
        Long productId = 1L;
        ProductRequest request = new ProductRequest();
        request.setName("  Updated Name  "); // Có whitespace
        request.setDescription("Updated Description");
        request.setPrice(new BigDecimal("399.99"));
        request.setQuantity(200);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productMapper).updateEntity(any(Product.class), any(ProductRequest.class));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.updateProduct(productId, request);

        // Assert
        assertNotNull(result);
        // Verify name was trimmed
        assertEquals("Updated Name", request.getName());

        // Verify workflow
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntity(any(Product.class), any(ProductRequest.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("Edge Case: Description có thể null (optional field)")
    void createProduct_WithNullDescription_ShouldSucceed() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Product Without Description");
        request.setDescription(null); // Description là optional
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(10);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Edge Case: Price với số thập phân chính xác")
    void createProduct_WithPreciseDecimalPrice_ShouldHandleCorrectly() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Precise Price Product");
        request.setDescription("Testing decimal precision");
        request.setPrice(new BigDecimal("123.45")); // 2 chữ số thập phân
        request.setQuantity(10);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Edge Case: Price gần giới hạn tối đa 999,999,999")
    void createProduct_WithLargePrice_ShouldSucceed() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Expensive Product");
        request.setDescription("High-end item");
        request.setPrice(new BigDecimal("999999999.99")); // Gần giới hạn
        request.setQuantity(1);

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Edge Case: Quantity gần giới hạn tối đa 99,999")
    void createProduct_WithLargeQuantity_ShouldSucceed() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Bulk Product");
        request.setDescription("Large stock");
        request.setPrice(new BigDecimal("9.99"));
        request.setQuantity(99999); // Giới hạn

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(testProductResponse);

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }
}