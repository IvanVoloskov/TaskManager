--liquibase formatted sql

--changeset ivan:005-task-created-at
ALTER TABLE task ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX idx_task_created_at ON task (created_at);
--rollback DROP INDEX idx_task_created_at;
--rollback ALTER TABLE task DROP COLUMN created_at;