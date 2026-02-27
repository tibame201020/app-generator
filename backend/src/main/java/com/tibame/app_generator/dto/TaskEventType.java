package com.tibame.app_generator.dto;

public enum TaskEventType {
    QUEUED,
    RUNNING,
    STEP_START,
    STEP_COMPLETE,
    PROGRESS,
    RETRY_SCHEDULED,
    COMPLETED,
    FAILED
}
