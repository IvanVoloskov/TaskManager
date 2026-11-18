package ru.practicum.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.taskmanager.dto.CategoryDto;
import ru.practicum.taskmanager.dto.CategoryResponse;
import ru.practicum.taskmanager.exception.ConflictException;
import ru.practicum.taskmanager.exception.NotFoundException;
import ru.practicum.taskmanager.mapper.CategoryMapper;
import ru.practicum.taskmanager.model.Category;
import ru.practicum.taskmanager.repository.CategoryRepository;
import ru.practicum.taskmanager.repository.TaskRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @CacheEvict(cacheNames = {"categories", "tasks"}, allEntries = true)
    public CategoryResponse create(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Категория '" + dto.name() + "' уже существует");
        }
        Category category = categoryRepository.save(categoryMapper.toEntity(dto));
        log.info("Создана категория id={}, name={}", category.getId(), category.getName());
        return categoryMapper.toResponse(category);
    }

    @Cacheable(cacheNames = "categories", key = "'all'")
    public List<CategoryResponse> getAll() {
        log.info("Список категорий загружен из БД (в кэше не было)");
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    @Cacheable(cacheNames = "categories", key = "#id")
    public CategoryResponse getById(Long id) {
        log.info("Категория id={} загружена из БД (в кэше не было)", id);
        return categoryMapper.toResponse(getEntity(id));
    }

    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));
    }

    @Transactional
    @CacheEvict(cacheNames = {"categories", "tasks"}, allEntries = true)
    public CategoryResponse update(Long id, CategoryDto dto) {
        Category category = getEntity(id);
        if (!category.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            throw new ConflictException("Категория '" + dto.name() + "' уже существует");
        }
        categoryMapper.updateEntity(category, dto);
        log.info("Обновлена категория id={}", id);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @CacheEvict(cacheNames = {"categories", "tasks"}, allEntries = true)
    public void delete(Long id) {
        getEntity(id);
        if (taskRepository.existsByCategoryId(id)) {
            throw new ConflictException("Нельзя удалить категорию id=" + id + ": в ней есть задачи");
        }
        categoryRepository.deleteById(id);
        log.info("Удалена категория id={}", id);
    }
}