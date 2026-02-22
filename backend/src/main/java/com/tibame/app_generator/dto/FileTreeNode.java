package com.tibame.app_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 檔案樹節點 DTO，用於呈現 Git Repository 中的檔案結構。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileTreeNode {

    /** 檔案或目錄名稱 */
    private String name;

    /** 相對於 repo root 的完整路徑 */
    private String path;

    /** 節點類型: "file" 或 "directory" */
    private String type;

    /** 子節點列表（僅 directory 有值） */
    private List<FileTreeNode> children;
}
