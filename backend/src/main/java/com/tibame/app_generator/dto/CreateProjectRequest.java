package com.tibame.app_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立專案請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    /** 專案名稱 */
    private String name;

    /** 專案描述 */
    private String description;

    /** 模板 ID（可選） */
    private String templateId;
}
