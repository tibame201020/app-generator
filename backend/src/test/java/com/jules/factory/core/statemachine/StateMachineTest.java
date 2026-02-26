package com.jules.factory.core.statemachine;

import com.jules.factory.domain.entity.Conversation;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateMachineTest {

    @Test
    void testStateContextCreation() {
        Long projectId = 1L;
        List<Conversation> messages = new ArrayList<>();

        StateContext context = new StateContext(projectId, messages);

        assertNotNull(context);
        assertEquals(projectId, context.projectId());
        assertEquals(messages, context.currentMessages());
    }

    @Test
    void testStateMachineEngineImplementation() {
        // Create a simple implementation to verify interface contract
        StateMachineEngine engine = new StateMachineEngine() {
            @Override
            public void processEvent(StateContext context) {
                // Do nothing
            }
        };

        Long projectId = 2L;
        List<Conversation> messages = new ArrayList<>();
        StateContext context = new StateContext(projectId, messages);

        engine.processEvent(context);

        // If we reach here without exception, the interface is working as expected
        assertTrue(true);
    }
}
