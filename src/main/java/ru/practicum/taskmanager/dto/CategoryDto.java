package ru.practicum.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(@NotBlank String name) {
}
