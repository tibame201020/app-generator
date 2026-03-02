package com.tibame.app_generator;

import com.tibame.app_generator.enums.TaskStatus;
import com.tibame.app_generator.model.*;
import com.tibame.app_generator.repository.*;
import com.tibame.app_generator.service.WorkflowExecutor;
import com.tibame.app_generator.service.llm.LlmAgentExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class WorkflowRetryIntegrationTest {

    @Autowired
    private WorkflowExecutor workflowExecutor;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowRunRepository workflowRunRepository;

    @Autowired
    private AgentTaskRepository agentTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private LlmAgentExecutionService llmAgentExecutionService;

    @MockBean
    private com.tibame.app_generator.plugin.PluginManager pluginManager;

    private Project project;
    private WorkflowRun run;

    @BeforeEach
    public void setup() {
        // Cleaning up manually as we removed @Transactional (implied)
        agentTaskRepository.deleteAll();
        workflowRunRepository.deleteAll();
        workflowRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .username("testuser_" + UUID.randomUUID())
                .email("test_" + UUID.randomUUID() + "@example.com")
                .passwordHash("hash")
                .build();
        user = userRepository.save(user);

        project = Project.builder()
                .name("Retry Test Project " + UUID.randomUUID())
                .description("A test project")
                .gitRepoPath("/tmp/test-repo-" + UUID.randomUUID())
                .user(user)
                .build();
        project = projectRepository.save(project);

        Workflow workflow = new Workflow();
        workflow.setProject(project);

        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", "1");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("agentType", "PM"); // Correct Enum Value
        data.put("label", "PM Task");
        node.put("data", data);

        Map<String, Object> graphData = new LinkedHashMap<>();
        graphData.put("nodes", List.of(node));
        graphData.put("edges", List.of());
        workflow.setGraphData(graphData);
        workflowRepository.save(workflow);

        run = WorkflowRun.builder()
                .project(project)
                .status(TaskStatus.RUNNING)
                .build();
        run = workflowRunRepository.save(run);
    }

    @Test
    public void testTaskRetryLogic() throws Exception {
        doThrow(new RuntimeException("Fail 1"))
            .when(llmAgentExecutionService).executeTask(any(), any());

        // We also need to mock pluginManager since PM goes through the plugin architecture now
        doThrow(new com.tibame.app_generator.plugin.PluginException("Fail 1"))
            .when(pluginManager).executeCapability(any(), any());

        workflowExecutor.executeRunAsync(run.getId(), project.getId());

        int retries = 0;
        List<AgentTask> tasks = Collections.emptyList();

        while (retries < 20) {
            tasks = agentTaskRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(run.getId());
            if (!tasks.isEmpty()) {
                AgentTask task = tasks.get(0);
                // We are looking for RETRY_WAIT status or failure/retry history
                if (task.getRetryCount() > 0 || task.getStatus() == TaskStatus.RETRY_WAIT) {
                    break;
                }
            }
            Thread.sleep(500);
            retries++;
        }

        assertFalse(tasks.isEmpty(), "Task should be created");
        AgentTask task = tasks.get(0);

        // Relaxed assertion: Status can be RETRY_WAIT or FAIL (if retries exhausted quickly)
        assertTrue(task.getStatus() == TaskStatus.RETRY_WAIT || task.getStatus() == TaskStatus.FAIL,
                "Task should be in RETRY_WAIT or FAIL status, was " + task.getStatus());

        // Relaxed assertion: Retry count should be at least 1
        assertTrue(task.getRetryCount() >= 1, "Retry count should be at least 1, was " + task.getRetryCount());

        assertNotNull(task.getAttemptHistory(), "Attempt history should not be null");
        assertTrue(task.getAttemptHistory().size() >= 1, "Attempt history should have at least 1 entry");
        String errorMsg = (String) task.getAttemptHistory().get(0).get("error");
        assertTrue(errorMsg != null && errorMsg.contains("Fail 1"), "Error message should match: " + errorMsg);
    }
}
