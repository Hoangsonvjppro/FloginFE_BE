package com.flogin.service.product;

import com.flogin.dto.product.CategoryRequest;
import com.flogin.dto.product.CategoryResponse;
import com.flogin.entity.product.Category;
import com.flogin.repository.product.CategoryRepository;
import com.flogin.exception.BadRequestException;
import com.flogin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Trim and validate
        request.setName(request.getName().trim());
        
        // Check if category already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }
        
        // Create category
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        
        Category savedCategory = categoryRepository.save(category);
        
        return toResponse(savedCategory);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        
        return toResponse(category);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        
        // Trim and validate
        request.setName(request.getName().trim());
        
        // Check if new name conflicts with existing category
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        
        Category updatedCategory = categoryRepository.save(category);
        
        return toResponse(updatedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found with id: " + id);
        }
        
        categoryRepository.deleteById(id);
    }
    
    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
