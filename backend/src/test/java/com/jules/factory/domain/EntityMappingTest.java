package com.jules.factory.domain;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import com.jules.factory.domain.enums.ProjectState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SnowflakeIdGenerator.class)
public class EntityMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Test
    public void testProjectAndConversationMapping() {
        // Create Project
        Long projectId = snowflakeIdGenerator.nextId();
        Project project = new Project(projectId, "Test Project", "Description");
        entityManager.persist(project);

        // Create Conversation
        Long conversationId = snowflakeIdGenerator.nextId();
        Conversation conversation = new Conversation(conversationId, project, "PM", "Hello", "http://minio/file.txt");
        entityManager.persist(conversation);

        entityManager.flush();
        entityManager.clear();

        // Retrieve Project
        Project foundProject = entityManager.find(Project.class, projectId);
        assertThat(foundProject).isNotNull();
        assertThat(foundProject.getName()).isEqualTo("Test Project");
        assertThat(foundProject.getStatus()).isEqualTo(ProjectState.REQUIREMENT_GATHERING);
        assertThat(foundProject.getCreatedAt()).isNotNull();

        // Retrieve Conversation
        Conversation foundConversation = entityManager.find(Conversation.class, conversationId);
        assertThat(foundConversation).isNotNull();
        assertThat(foundConversation.getProject().getId()).isEqualTo(projectId);
        assertThat(foundConversation.getContentText()).isEqualTo("Hello");
        assertThat(foundConversation.getSenderRole()).isEqualTo("PM");
        assertThat(foundConversation.getCreatedAt()).isNotNull();
    }
}
