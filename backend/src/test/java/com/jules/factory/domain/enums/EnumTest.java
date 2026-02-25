package com.jules.factory.domain.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EnumTest {

    @Test
    void testAgentRoleValues() {
        assertThat(AgentRole.values()).containsExactlyInAnyOrder(
            AgentRole.USER,
            AgentRole.PM,
            AgentRole.UIUX,
            AgentRole.SA,
            AgentRole.PG,
            AgentRole.SYSTEM
        );
    }

    @Test
    void testProjectStateValues() {
        assertThat(ProjectState.values()).containsExactlyInAnyOrder(
            ProjectState.REQUIREMENT_GATHERING,
            ProjectState.ARCHITECTURE_DESIGN,
            ProjectState.IMPLEMENTATION,
            ProjectState.REVIEW
        );
    }
}
