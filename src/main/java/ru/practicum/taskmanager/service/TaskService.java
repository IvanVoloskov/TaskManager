package ru.practicum.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.practicum.taskmanager.dto.TaskDto;
import ru.practicum.taskmanager.dto.TaskResponse;
import ru.practicum.taskmanager.exception.NotFoundException;
import ru.practicum.taskmanager.mapper.TaskMapper;
import ru.practicum.taskmanager.model.Task;
import ru.practicum.taskmanager.model.TaskStatus;
import ru.practicum.taskmanager.repository.TaskRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;

    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public TaskResponse create(TaskDto dto) {
        Task task = taskMapper.toEntity(
                dto,
                userService.getEntity(dto.userId()),
                categoryService.getEntity(dto.categoryId()));
        Task saved = taskRepository.save(task);
        log.info("Создана задача id={}, status={}, userId={}, categoryId={}",
                saved.getId(), saved.getStatus(), dto.userId(), dto.categoryId());
        return taskMapper.toResponse(saved);
    }

    @Cacheable(cacheNames = "tasks", key = "'list:' + #categoryId + ':' + #status")
    public List<TaskResponse> getAll(Long categoryId, TaskStatus status) {
        log.info("Список задач загружен из БД (в кэше не было): categoryId={}, status={}", categoryId, status);
        List<Task> tasks;
        if (categoryId != null && status != null) {
            tasks = taskRepository.findByCategoryIdAndStatus(categoryId, status);
        } else if (categoryId != null) {
            tasks = taskRepository.findByCategoryId(categoryId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Cacheable(cacheNames = "tasks", key = "#id")
    public TaskResponse getById(Long id) {
        log.info("Задача id={} загружена из БД (в кэше не было)", id);
        return taskMapper.toResponse(getEntity(id));
    }

    private Task getEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задача с id=" + id + " не найдена"));
    }

    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public TaskResponse update(Long id, TaskDto dto) {
        Task task = getEntity(id);
        taskMapper.updateEntity(
                task,
                dto,
                userService.getEntity(dto.userId()),
                categoryService.getEntity(dto.categoryId()));
        log.info("Обновлена задача id={}, status={}", id, task.getStatus());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public void delete(Long id) {
        getEntity(id);
        taskRepository.deleteById(id);
        log.info("Удалена задача id={}", id);
    }
}