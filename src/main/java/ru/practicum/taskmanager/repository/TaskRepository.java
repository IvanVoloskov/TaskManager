package ru.practicum.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.taskmanager.model.Task;
import ru.practicum.taskmanager.model.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCategoryId(Long categoryId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByCategoryIdAndStatus(Long categoryId, TaskStatus status);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByUserId(Long userId);
}
