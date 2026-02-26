package com.tibame.app_generator.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodDTO {
    private String name;
    private String returnType;
    private List<String> parameters;
    private List<String> modifiers;
}
