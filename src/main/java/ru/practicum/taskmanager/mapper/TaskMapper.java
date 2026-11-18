package ru.practicum.taskmanager.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.taskmanager.dto.TaskDto;
import ru.practicum.taskmanager.dto.TaskResponse;
import ru.practicum.taskmanager.event.TaskEvent;
import ru.practicum.taskmanager.event.TaskEventType;
import ru.practicum.taskmanager.model.Category;
import ru.practicum.taskmanager.model.Task;
import ru.practicum.taskmanager.model.User;

import java.time.Instant;

@Component
public class TaskMapper {

    public Task toEntity(TaskDto dto, User user, Category category) {
        Task task = new Task(dto.title(), user, category);
        task.setDescription(dto.description());
        if (dto.status() != null) {
            task.setStatus(dto.status());
        }
        return task;
    }

    public void updateEntity(Task task, TaskDto dto, User user, Category category) {
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        if (dto.status() != null) {
            task.setStatus(dto.status());
        }
        task.setUser(user);
        task.setCategory(category);
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getUser().getId(),
                task.getUser().getName(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                task.getCreatedAt());
    }

    public TaskEvent toEvent(Task task, TaskEventType type) {
        return new TaskEvent(
                type,
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getUser().getId(),
                task.getCategory().getId(),
                Instant.now());
    }
}