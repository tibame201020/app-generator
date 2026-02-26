package com.jules.factory.service;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.core.statemachine.StateContext;
import com.jules.factory.core.statemachine.StateMachineEngine;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.AgentRole;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ConversationRepository;
import com.jules.factory.domain.repository.ProjectRepository;
import com.jules.factory.dto.ChatRequest;
import com.jules.factory.dto.CreateProjectRequest;
import com.jules.factory.dto.ProjectResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final StateMachineEngine stateMachineEngine;

    public ProjectService(ProjectRepository projectRepository,
                          ConversationRepository conversationRepository,
                          SnowflakeIdGenerator snowflakeIdGenerator,
                          StateMachineEngine stateMachineEngine) {
        this.projectRepository = projectRepository;
        this.conversationRepository = conversationRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.stateMachineEngine = stateMachineEngine;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        long projectId = snowflakeIdGenerator.nextId();
        Project project = new Project(projectId, request.name(), request.description());
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        Project savedProject = projectRepository.save(project);

        return new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getStatus(),
                savedProject.getCreatedAt()
        );
    }

    public void processChat(Long projectId, ChatRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        // Create and save user conversation
        long conversationId = snowflakeIdGenerator.nextId();
        Conversation conversation = new Conversation(
                conversationId,
                project,
                AgentRole.USER.name(),
                request.content(),
                null
        );
        conversationRepository.save(conversation);

        // Fetch conversation history for context
        List<Conversation> history = conversationRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        // Trigger state machine
        stateMachineEngine.processEvent(new StateContext(projectId, history));
    }
}
