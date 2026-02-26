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
public class ClassDTO {
    private String name;
    private String type; // CLASS, INTERFACE, ENUM
    private List<String> modifiers;
    private List<FieldDTO> fields;
    private List<MethodDTO> methods;
    private List<String> dependencies; // Imports
}
