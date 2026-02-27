package com.tibame.app_generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibame.app_generator.model.Project;
import com.tibame.app_generator.model.User;
import com.tibame.app_generator.model.Workflow;
import com.tibame.app_generator.repository.ProjectRepository;
import com.tibame.app_generator.repository.UserRepository;
import com.tibame.app_generator.repository.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class WorkflowJsonTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testWorkflowJsonPersistence() {
        User user = User.builder()
                .username("jsonuser_" + UUID.randomUUID())
                .email("json_" + UUID.randomUUID() + "@example.com")
                .passwordHash("hash")
                .build();
        user = userRepository.save(user);

        Project project = Project.builder()
                .name("JSON Test Project")
                .description("Test")
                .gitRepoPath("/tmp/json")
                .user(user)
                .build();
        project = projectRepository.save(project);

        Workflow workflow = new Workflow();
        workflow.setProject(project);

        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        data.put("number", 123);
        data.put("list", Arrays.asList("a", "b"));

        workflow.setGraphData(data);

        workflow = workflowRepository.save(workflow);

        // Clear cache/session to force DB fetch
        // In @Transactional test, L1 cache might return the same instance.
        // We can use flush/clear on EntityManager if we injected it, but repository.saveAndFlush might help.
        workflowRepository.flush();

        Workflow fetched = workflowRepository.findById(workflow.getId()).orElseThrow();
        assertNotNull(fetched.getGraphData());
        System.out.println("Fetched Graph Data: " + fetched.getGraphData());
        System.out.println("Type: " + fetched.getGraphData().getClass().getName());

        assertEquals("value", fetched.getGraphData().get("key"));
    }
}
