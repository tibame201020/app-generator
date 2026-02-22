package com.tibame.app_generator.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private List<String> contextFiles;
}
