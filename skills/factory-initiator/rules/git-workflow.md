# 🏭 Git Workflow Rules

本文件定義了 Worker Agent 進行版本控制的行為標準。

## 1. Conventional Commits 格式 [REQUIRED]
所有 commit 訊息必須遵循以下格式：
```
<type>: <description>
```
### 允許的類型 (Types):
- `feat`: 新功能實作
- `fix`: 修補錯誤
- `refactor`: 重構代碼 (不改變功能)
- `docs`: 僅更新文件
- `test`: 增加或修正測試
- `chore`: 基礎設施更新、依賴更新
- `perf`: 效能優化

## 2. 分支策略 (Branching)
- 每個任務必須在其專屬分支執行：`{{AGENT_NAME}}/task-{id}`。
- 禁止直接提交至 `{{BASE_BRANCH}}`。

## 3. PR 提交規範
- **標題**：`[{{AGENT_NAME}}] {task_title}`。
- **內容**：必須包含測試摘要與影響範圍說明。
- **標籤**：必須包含 `[auto-merge]` 標籤以觸發自動合併。
