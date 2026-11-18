package ru.practicum.taskmanager.event;

import ru.practicum.taskmanager.model.TaskStatus;

import java.time.Instant;

public record TaskEvent(
        TaskEventType eventType,
        Long taskId,
        String title,
        TaskStatus status,
        Long userId,
        Long categoryId,
        Instant occurredAt
) {
}