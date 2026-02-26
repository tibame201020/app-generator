package com.tibame.app_generator.service.llm;

import com.tibame.app_generator.enums.AgentType;
import lombok.Getter;

@Getter
public enum AgentPromptTemplate {

    PM(AgentType.PM, """
        You are an experienced Project Manager.
        Analyze the following project description and generate detailed requirements.

        Project Description:
        {{input}}

        Output must be in strict JSON format with the following structure:
        {
          "summary": "A brief summary of the requirements",
          "requirements": [
            { "id": "REQ-001", "title": "...", "description": "..." }
          ],
          "userStories": [
            { "id": "US-001", "role": "...", "action": "...", "benefit": "..." }
          ]
        }
        """),

    SA(AgentType.SA, """
        You are a skilled Software Architect.
        Based on the following requirements, design the system architecture and API.

        Requirements:
        {{input}}

        Output must be in strict JSON format with the following structure:
        {
          "summary": "Overview of the architecture",
          "databaseSchema": "Description or SQL of the schema",
          "apiEndpoints": [
            { "method": "GET", "path": "/api/...", "description": "..." }
          ],
          "componentDesign": "Description of components"
        }
        """),

    PG(AgentType.PG, """
        You are a proficient Programmer.
        Implement the following system design in the specified technology stack.

        Design:
        {{input}}

        Output must be in strict JSON format with the following structure:
        {
          "summary": "Summary of implemented features",
          "files": [
            { "path": "src/main/java/com/example/Demo.java", "content": "public class Demo { ... }" }
          ]
        }
        IMPORTANT: Escape all double quotes within the content string properly.
        """),

    QA(AgentType.QA, """
        You are a thorough QA Engineer.
        Review the following code and generate a test plan and quality report.

        Code:
        {{input}}

        Output must be in strict JSON format with the following structure:
        {
          "summary": "Overall quality assessment",
          "testCases": [
            { "id": "TC-001", "description": "...", "expectedResult": "..." }
          ],
          "codeReview": [
            { "file": "...", "issues": ["..."] }
          ]
        }
        """);

    private final AgentType agentType;
    private final String template;

    AgentPromptTemplate(AgentType agentType, String template) {
        this.agentType = agentType;
        this.template = template;
    }

    public static String getTemplate(AgentType type) {
        for (AgentPromptTemplate t : values()) {
            if (t.agentType == type) {
                return t.template;
            }
        }
        throw new IllegalArgumentException("No template found for agent type: " + type);
    }
}
