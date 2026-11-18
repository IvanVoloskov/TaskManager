package ru.practicum.taskmanager.dto;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

// Собственный «конверт» вместо Page: его стабильно сериализует и JSON, и Redis,
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) implements Serializable {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                List.copyOf(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}