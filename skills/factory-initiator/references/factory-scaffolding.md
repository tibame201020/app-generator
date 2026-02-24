# 🏗️ Initiator: Factory Scaffolding

這是啟動生產線的最後一步，負責實體化目錄與協議。

## 🛠️ 執行指南

### 1. 產出執行目錄
- 讀取 `../assets/templates/tracker.json`，將 Planner 拆解的任務寫入，存為 `.{{AGENT_NAME}}/tracker.json`。
- 讀取 `../assets/templates/AGENT_PROTOCOL.md`，替換變數後存為 `.{{AGENT_NAME}}/AGENT_PROTOCOL.md`。

### 2. 初始化核心文件與知識庫
- **建立目錄**：若不存在，則建立 `docs/` 與 `docs/ADR/`。
- **部署規則庫**：將 `../rules/*` 複製至 `.agents/rules/`。
  - **重要**：必須對 `git-workflow.md` 等包含變數的檔案執行與協議相同的變數替換（如 `{{AGENT_NAME}}`, `{{BASE_BRANCH}}`），確保規範完全符合當前專案環境。
- **初始化索引**：將 `../assets/templates/doc-categories.md` 複製至 `docs/doc-categories.md`。
- **產出占位文件**：根據 Architect 在階段 1 的規劃，建立對應檔案。
  - **內容規範**：占位文件不得為空，必須包含 `# [文件名]` 標題、`## Status: Pending` 狀態，以及一段說明「本文件由 Initiator 置放，等待后續任務填充詳情」。

### 3. 部署自動化裁判所
- 從 `../assets/templates/auto-merge.yml` 複製並生成 `.github/workflows/{{AGENT_NAME}}-auto-merge.yml`。

---
> 🎉 **完成**：告知使用者軟體工廠已就緒！
