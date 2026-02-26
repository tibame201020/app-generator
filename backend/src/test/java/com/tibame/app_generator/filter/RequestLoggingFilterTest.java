package com.tibame.app_generator.filter;

import com.tibame.app_generator.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RequestLoggingFilterTest {

    private MockMvc mockMvc;

    @InjectMocks
    private RequestLoggingFilter requestLoggingFilter;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(requestLoggingFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testRequestIdGeneratedAndReturned() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    public void testExistingRequestIdPreserved() throws Exception {
        String existingRequestId = "12345-abcde";
        mockMvc.perform(get("/test/ok").header("X-Request-ID", existingRequestId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", existingRequestId));
    }

    @Test
    public void testExceptionResponseContainsRequestId() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_BAD_REQUEST"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @RestController
    static class TestController {
        @GetMapping("/test/ok")
        public String ok() {
            return "OK";
        }

        @GetMapping("/test/error")
        public void error() {
            throw new IllegalArgumentException("Test Error");
        }
    }
}
