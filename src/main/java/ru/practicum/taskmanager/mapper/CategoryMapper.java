package ru.practicum.taskmanager.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.taskmanager.dto.CategoryDto;
import ru.practicum.taskmanager.dto.CategoryResponse;
import ru.practicum.taskmanager.model.Category;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryDto dto) {
        return new Category(dto.name());
    }

    public void updateEntity(Category category, CategoryDto dto) {
        category.setName(dto.name());
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}