package ru.practicum.taskmanager.dto;

import java.io.Serializable;

public record CategoryResponse(Long id, String name) implements Serializable {
}