package com.jules.factory.domain.repository;

import com.jules.factory.common.util.SnowflakeIdGenerator;
import com.jules.factory.domain.entity.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SnowflakeIdGenerator.class)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Test
    void testSaveAndFindProject() {
        Project project = new Project();
        Long id = snowflakeIdGenerator.nextId();
        project.setId(id);
        project.setName("Test Project");
        project.setDescription("Description");

        projectRepository.save(project);

        Project found = projectRepository.findById(id).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("Test Project");
    }
}
