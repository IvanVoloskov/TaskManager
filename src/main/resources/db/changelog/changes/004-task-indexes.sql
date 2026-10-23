--liquibase formatted sql

--changeset ivan:004-task-indexes
CREATE INDEX idx_task_status ON task (status);
CREATE INDEX idx_task_category_id ON task (category_id);
CREATE INDEX idx_task_user_id ON task (user_id);
--rollback DROP INDEX idx_task_status;
--rollback DROP INDEX idx_task_category_id;
--rollback DROP INDEX idx_task_user_id;