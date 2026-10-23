package ru.practicum.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.practicum.taskmanager.model.TaskStatus;

public record TaskDto(@NotBlank @Size(max = 120) String title, @Size(max = 2000) String description,
                      TaskStatus status, @NotNull Long userId, @NotNull Long categoryId) {
}
