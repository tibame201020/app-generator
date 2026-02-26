package com.jules.factory.core.statemachine;

import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.ProjectState;
import com.jules.factory.domain.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class StateMachineEngineImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private StateHandler stateHandler;

    private StateMachineEngineImpl engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        engine = new StateMachineEngineImpl(Collections.singletonList(stateHandler), projectRepository);
    }

    @Test
    void processEvent_ShouldDelegateToCorrectHandler() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REQUIREMENT_GATHERING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(stateHandler.supports(ProjectState.REQUIREMENT_GATHERING)).thenReturn(true);

        StateContext context = new StateContext(projectId, Collections.emptyList());
        engine.processEvent(context);

        verify(stateHandler).handle(context);
    }

    @Test
    void processEvent_ShouldLogWarningIfNoHandlerFound() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setStatus(ProjectState.REVIEW);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(stateHandler.supports(any())).thenReturn(false);

        StateContext context = new StateContext(projectId, Collections.emptyList());
        engine.processEvent(context);

        verify(stateHandler, never()).handle(any());
    }

    @Test
    void processEvent_ShouldLogErrorIfProjectNotFound() {
        Long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        StateContext context = new StateContext(projectId, Collections.emptyList());
        engine.processEvent(context);

        verify(stateHandler, never()).handle(any());
    }
}
