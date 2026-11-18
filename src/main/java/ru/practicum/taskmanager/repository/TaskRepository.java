package ru.practicum.taskmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.taskmanager.model.Task;
import ru.practicum.taskmanager.model.TaskStatus;

import java.time.Instant;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // categoryId и status можно передать как null — тогда они не фильтруют.
    // createdFrom и createdTo передавать как null НЕЛЬЗЯ: Postgres не может определить тип параметра
    // в конструкции "? is null" для дат (ошибка 42P18). Сервис подставляет вместо null крайние даты.
    // createdFrom — включительно, createdTo — НЕ включительно.
    // @EntityGraph подтягивает user и category тем же запросом (иначе будет N+1).
    @EntityGraph(attributePaths = {"user", "category"})
    @Query("""
            select t from Task t
            where (:categoryId is null or t.category.id = :categoryId)
              and (:status is null or t.status = :status)
              and t.createdAt >= :createdFrom
              and t.createdAt < :createdTo
            """)
    Page<Task> search(@Param("categoryId") Long categoryId,
                      @Param("status") TaskStatus status,
                      @Param("createdFrom") Instant createdFrom,
                      @Param("createdTo") Instant createdTo,
                      Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByUserId(Long userId);
}
