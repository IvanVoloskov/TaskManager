package ru.practicum.taskmanager.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.taskmanager.dto.PageResponse;
import ru.practicum.taskmanager.dto.TaskDto;
import ru.practicum.taskmanager.dto.TaskResponse;
import ru.practicum.taskmanager.model.TaskStatus;
import ru.practicum.taskmanager.service.TaskService;

import java.time.Instant;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Создана не раньше (включительно), ISO-8601, напр. 2026-11-01T00:00:00Z")
            @RequestParam(required = false) Instant createdFrom,
            @Parameter(description = "Создана раньше (не включительно), ISO-8601")
            @RequestParam(required = false) Instant createdTo,
            @Parameter(description = "Номер страницы, с нуля") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, 1..100") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(taskService.getAll(categoryId, status, createdFrom, createdTo, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}