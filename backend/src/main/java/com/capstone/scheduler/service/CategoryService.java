package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.CategoryDto;
import com.capstone.scheduler.model.Category;
import com.capstone.scheduler.model.User;
import com.capstone.scheduler.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuthService authService;

    public CategoryService(CategoryRepository categoryRepository, AuthService authService) {
        this.categoryRepository = categoryRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return categoryRepository.findByUser(currentUser).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        
        // Prevent duplicate category names per user
        Category existing = categoryRepository.findByNameAndUser(dto.getName(), currentUser).orElse(null);
        if (existing != null) {
            return toDto(existing);
        }

        Category category = Category.builder()
                .name(dto.getName())
                .color(dto.getColor() != null ? dto.getColor() : "#4F46E5")
                .user(currentUser)
                .build();

        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized action to delete this category.");
        }
        
        categoryRepository.delete(category);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .build();
    }
}
