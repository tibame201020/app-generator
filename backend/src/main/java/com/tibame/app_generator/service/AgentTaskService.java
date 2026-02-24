package com.tibame.app_generator.service;

import com.tibame.app_generator.dto.TaskEventDTO;
import com.tibame.app_generator.dto.TaskEventType;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.AgentTaskRepository;
import com.tibame.app_generator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentTaskService {

    private final AgentTaskRepository agentTaskRepository;
    private final ProjectRepository projectRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AgentTask createTask(UUID projectId, AgentType agentType, String taskName, Map<String, Object> contextData) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        AgentTask task = AgentTask.builder()
                .project(project)
                .agentType(agentType)
                .taskName(taskName)
                .status(TaskStatus.PENDING)
                .contextData(contextData)
                .progressPct(0)
                .logContent("")
                .build();

        task = agentTaskRepository.save(task);

        publishEvent(task, TaskEventType.QUEUED, "Task created and queued.");
        return task;
    }

    @Transactional
    public void startTask(UUID taskId) {
        AgentTask task = getTask(taskId);
        task.setStatus(TaskStatus.RUNNING);
        agentTaskRepository.save(task);
        publishEvent(task, TaskEventType.RUNNING, "Task started.");
    }

    @Transactional
    public void updateProgress(UUID taskId, int progressPct, String logMessage) {
        AgentTask task = getTask(taskId);
        task.setProgressPct(progressPct);
        if (logMessage != null && !logMessage.isEmpty()) {
            String currentLog = task.getLogContent() == null ? "" : task.getLogContent();
            task.setLogContent(currentLog + "\n" + logMessage);
        }
        agentTaskRepository.save(task);
        publishEvent(task, TaskEventType.PROGRESS, logMessage);
    }

    @Transactional
    public void completeTask(UUID taskId, String resultMessage) {
        AgentTask task = getTask(taskId);
        task.setStatus(TaskStatus.SUCCESS);
        task.setProgressPct(100);
        if (resultMessage != null) {
            String currentLog = task.getLogContent() == null ? "" : task.getLogContent();
            task.setLogContent(currentLog + "\n" + resultMessage);
        }
        agentTaskRepository.save(task);
        publishEvent(task, TaskEventType.COMPLETED, resultMessage);
    }

    @Transactional
    public void failTask(UUID taskId, String errorMessage) {
        AgentTask task = getTask(taskId);
        task.setStatus(TaskStatus.FAIL);
        String currentLog = task.getLogContent() == null ? "" : task.getLogContent();
        task.setLogContent(currentLog + "\nERROR: " + errorMessage);
        agentTaskRepository.save(task);
        publishEvent(task, TaskEventType.FAILED, errorMessage);
    }

    public AgentTask getTask(UUID taskId) {
        return agentTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    public java.util.List<AgentTask> getTasksByProject(UUID projectId) {
        return agentTaskRepository.findByProject_Id(projectId);
    }

    private void publishEvent(AgentTask task, TaskEventType type, String message) {
        TaskEventDTO event = TaskEventDTO.builder()
                .type(type)
                .projectId(task.getProject().getId())
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .progress(task.getProgressPct())
                .message(message)
                .payload(task.getContextData())
                .timestamp(ZonedDateTime.now())
                .build();

        String destination = "/topic/project/" + task.getProject().getId() + "/tasks";
        log.info("Publishing event {} to {}", type, destination);
        messagingTemplate.convertAndSend(destination, event);
    }
}
