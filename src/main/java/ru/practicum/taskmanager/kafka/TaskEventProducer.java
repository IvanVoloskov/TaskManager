package ru.practicum.taskmanager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.taskmanager.event.TaskEvent;

@Slf4j
@Component
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final String topic;

    public TaskEventProducer(KafkaTemplate<String, TaskEvent> kafkaTemplate,
                             @Value("${app.kafka.topic.task-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(TaskEvent event) {
        // Ключ = id задачи: все события одной задачи попадут в одну партицию
        // и будут прочитаны строго по порядку
        String key = String.valueOf(event.taskId());

        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Событие {} по задаче id={} отправлено в Kafka: partition={}, offset={}",
                        event.eventType(),
                        event.taskId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Не удалось отправить событие {} по задаче id={}",
                        event.eventType(), event.taskId(), ex);
            }
        });
    }
}