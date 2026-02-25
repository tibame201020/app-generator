package com.jules.factory.domain.repository;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Conversation;
import com.jules.factory.domain.entity.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SnowflakeIdGenerator.class)
class ConversationRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Test
    void testFindByProjectIdOrderByCreatedAtAsc() {
        // Create Project
        Project project = new Project();
        Long projectId = snowflakeIdGenerator.nextId();
        project.setId(projectId);
        project.setName("Chat Project");
        projectRepository.save(project);

        // Create Conversation 1
        Conversation conv1 = new Conversation();
        Long conv1Id = snowflakeIdGenerator.nextId();
        conv1.setId(conv1Id);
        conv1.setProject(project);
        conv1.setContentText("Hello");
        conv1.setSenderRole("USER");
        conversationRepository.save(conv1);

        // Create Conversation 2
        Conversation conv2 = new Conversation();
        Long conv2Id = snowflakeIdGenerator.nextId();
        conv2.setId(conv2Id);
        conv2.setProject(project);
        conv2.setContentText("Hi there");
        conv2.setSenderRole("AI");
        conversationRepository.save(conv2);

        // Fetch
        List<Conversation> conversations = conversationRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        assertThat(conversations).hasSize(2);
        assertThat(conversations.get(0).getId()).isEqualTo(conv1Id);
        assertThat(conversations.get(1).getId()).isEqualTo(conv2Id);
        // Snowflake IDs are roughly ordered by time, so conv1 should be first if generated first.
        // Also createdAt is set by @PrePersist.
    }
}
