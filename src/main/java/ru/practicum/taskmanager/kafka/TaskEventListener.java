package ru.practicum.taskmanager.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.practicum.taskmanager.event.TaskEvent;

/**
 * Отправляет событие в Kafka только ПОСЛЕ успешного коммита транзакции.
 * Если транзакция откатилась — событие тихо отбрасывается, фантомных событий нет.
 * Ограничение: если приложение упадёт между коммитом и отправкой, событие потеряется
 * (полностью это закрывает только Transactional Outbox).
 */
@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TaskEventProducer taskEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEvent(TaskEvent event) {
        taskEventProducer.send(event);
    }
}
