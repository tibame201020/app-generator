package com.jules.factory.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiTestController {

    private final ChatModel chatModel;

    public AiTestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/test-ai")
    public Map<String, String> testAi(@RequestParam(value = "msg", defaultValue = "hello") String msg) {
        String response = chatModel.call(msg);
        return Map.of("response", response);
    }
}
