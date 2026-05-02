package com.musicstore.product.service;

import com.musicstore.product.api.v1.dto.CategoryRequest;
import com.musicstore.product.api.v1.dto.CategoryResponse;
import com.musicstore.product.domain.entity.Category;
import com.musicstore.product.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findByParentIsNull().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapper.toResponse(categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id)));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
            .name(request.name())
            .build();

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + request.parentId()));
            category.setParent(parent);
        }

        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

        category.setName(request.name());

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + request.parentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}