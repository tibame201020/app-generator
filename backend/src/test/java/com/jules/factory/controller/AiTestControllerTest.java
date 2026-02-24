package com.jules.factory.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiTestController.class)
class AiTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatModel chatModel;

    @Test
    void testAiEndpoint() throws Exception {
        // Mock the ChatModel response
        when(chatModel.call(anyString())).thenReturn("I am a mocked AI response");

        // Perform GET request
        mockMvc.perform(get("/api/test-ai").param("msg", "Hi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("I am a mocked AI response"));
    }
}
