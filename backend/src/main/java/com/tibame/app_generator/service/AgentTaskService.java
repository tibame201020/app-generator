package com.tibame.app_generator.service;

import com.tibame.app_generator.dto.ChatRequest;
import com.tibame.app_generator.enums.AgentType;
import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.AgentTask;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.repository.AgentTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentTaskService {

    private final AgentTaskRepository agentTaskRepository;
    private final ProjectService projectService;

    @Transactional
    public AgentTask createTask(UUID projectId, ChatRequest request) {
        Project project = projectService.getProjectById(projectId);

        Map<String, Object> context = new HashMap<>();
        context.put("message", request.getMessage());
        context.put("contextFiles", request.getContextFiles());

        AgentTask task = AgentTask.builder()
                .project(project)
                .agentType(AgentType.PM)
                .taskName("Chat request")
                .status(TaskStatus.PENDING)
                .contextData(context)
                .progressPct(0)
                .build();

        return agentTaskRepository.save(task);
    }

    public List<AgentTask> getTasksByProject(UUID projectId) {
        projectService.getProjectById(projectId);
        return agentTaskRepository.findByProjectId(projectId);
    }
}
