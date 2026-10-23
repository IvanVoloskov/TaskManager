package ru.practicum.taskmanager.dto;

import java.io.Serializable;

public record UserResponse(Long id, String name, String email) implements Serializable {
}