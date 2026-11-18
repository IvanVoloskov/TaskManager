package ru.practicum.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.taskmanager.dto.PageResponse;
import ru.practicum.taskmanager.dto.TaskDto;
import ru.practicum.taskmanager.dto.TaskResponse;
import ru.practicum.taskmanager.event.TaskEventType;
import ru.practicum.taskmanager.exception.BadRequestException;
import ru.practicum.taskmanager.exception.NotFoundException;
import ru.practicum.taskmanager.mapper.TaskMapper;
import ru.practicum.taskmanager.model.Task;
import ru.practicum.taskmanager.model.TaskStatus;
import ru.practicum.taskmanager.repository.TaskRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // по умолчанию все методы только читают; записывающие помечены ниже
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Instant FAR_FUTURE = Instant.parse("9999-01-01T00:00:00Z");

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;
    // Публикуем внутреннее событие Spring; в Kafka оно уйдёт после коммита (см. TaskEventListener)
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public TaskResponse create(TaskDto dto) {
        Task task = taskMapper.toEntity(
                dto,
                userService.getEntity(dto.userId()),
                categoryService.getEntity(dto.categoryId()));
        Task saved = taskRepository.save(task);
        log.info("Создана задача id={}, status={}, userId={}, categoryId={}",
                saved.getId(), saved.getStatus(), dto.userId(), dto.categoryId());

        eventPublisher.publishEvent(taskMapper.toEvent(saved, TaskEventType.CREATED));
        return taskMapper.toResponse(saved);
    }

    // Ключ кэша включает ВСЕ параметры, иначе разные запросы получат чужой результат.
    @Cacheable(cacheNames = "tasks",
            key = "'list:' + #categoryId + ':' + #status + ':' + #createdFrom + ':' + #createdTo"
                    + " + ':' + #page + ':' + #size")
    public PageResponse<TaskResponse> getAll(Long categoryId, TaskStatus status,
                                             Instant createdFrom, Instant createdTo,
                                             int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size должен быть от 1 до " + MAX_PAGE_SIZE);
        }
        if (createdFrom != null && createdTo != null && !createdFrom.isBefore(createdTo)) {
            throw new BadRequestException("createdFrom должен быть раньше createdTo");
        }
        log.info("Список задач загружен из БД (в кэше не было): categoryId={}, status={}, from={}, to={}, page={}, size={}",
                categoryId, status, createdFrom, createdTo, page, size);

        // Сортировку задаём сами, а не берём из запроса: так клиент не сможет сортировать по произвольному полю.
        // id добавлен вторым ключом, чтобы порядок был стабильным и страницы не «плыли».
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        // Вместо null — крайние даты (см. комментарий в TaskRepository.search)
        var from = createdFrom != null ? createdFrom : Instant.EPOCH;
        var to = createdTo != null ? createdTo : FAR_FUTURE;
        var result = taskRepository.search(categoryId, status, from, to, pageable);
        return PageResponse.from(result.map(taskMapper::toResponse));
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

    @Transactional
    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public TaskResponse update(Long id, TaskDto dto) {
        Task task = getEntity(id);
        taskMapper.updateEntity(
                task,
                dto,
                userService.getEntity(dto.userId()),
                categoryService.getEntity(dto.categoryId()));

        Task saved = taskRepository.save(task);
        log.info("Обновлена задача id={}, status={}", id, task.getStatus());
        eventPublisher.publishEvent(taskMapper.toEvent(saved, TaskEventType.UPDATED));
        return taskMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public void delete(Long id) {
        Task task = getEntity(id);
        taskRepository.deleteById(id);
        log.info("Удалена задача id={}", id);
        eventPublisher.publishEvent(taskMapper.toEvent(task, TaskEventType.DELETED));
    }
}