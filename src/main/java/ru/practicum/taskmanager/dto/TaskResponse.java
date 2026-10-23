package ru.practicum.taskmanager.dto;

import ru.practicum.taskmanager.model.TaskStatus;

import java.io.Serializable;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Long userId,
        String userName,
        Long categoryId,
        String categoryName
) implements Serializable {
}