# 🏗️ Initiator: Factory Scaffolding

這是啟動生產線的最後一步，負責實體化目錄與協議。

## 🛠️ 執行指南

### 1. 產出執行目錄
- 讀取 `../assets/templates/tracker.json`，將 Planner 拆解的任務寫入，存為 `.{{AGENT_NAME}}/tracker.json`。
- 讀取 `../assets/templates/AGENT_PROTOCOL.md`，替換變數後存為 `.{{AGENT_NAME}}/AGENT_PROTOCOL.md`。

### 2. 初始化核心文件與知識庫
- **建立目錄**：若不存在，則建立 `docs/`、`docs/ADR/` 與 `specs/tasks/`。
- **部署規則庫 (Issue 5 Fix)**：由 Initiator (LLM) 根據 `skills/factory-initiator/rules/*` 的內容，直接將其「生成」至目標專案的 `.agents/rules/`。
  - **重要**：這不是 OS 層級的 `cp` 命令，而是 LLM 的內容寫入行為。必須對 `git-workflow.md` 等包含變數的檔案執行與協議相同的變數替換（如 `{{AGENT_NAME}}`, `{{BASE_BRANCH}}`），確保規範完全符合當前專案環境。
- **初始化索引**：將 `../assets/templates/doc-categories.md` 複製至 `docs/doc-categories.md`。
- **產出占位文件**：根據 Architect 在階段 1 的規劃，建立對應檔案。
  - **重要**：必須生成 `docs/doc-categories.md` 骨架，防止 Worker 卡死。
  - **內容規範**：占位文件不得為空，包含 `# [文件名]` 標題與 `## Status: Pending`。

### 3. 部署自動化裁判所
- 從 `skills/factory-initiator/assets/templates/auto-merge.yml` 複製並生成 `.github/workflows/{{AGENT_NAME}}-auto-merge.yml`。
  - ⚠️ **技術棧提醒 (Issue 3 Fix)**：CI 模板預設為 **Java/Maven + NPM**。Initiator 必須根據 Architect 決定的技術棧（如 Python/Go/Node），動態替換 YAML 中的 build 與 test 指令，嚴禁無腦套用。
- 從 `skills/factory-initiator/assets/templates/cleanup-stale-tasks.yml` 複製並生成 `.github/workflows/{{AGENT_NAME}}-cleanup.yml`。

### 4. 手動配置清單 (Manual Action Required)
- 您必須告知使用者執行以下操作，否則自動化生產線將無法合併程式碼：
  1. **開啟開關**：Repo Settings -> General -> ✅ Allow auto-merge。
  2. **建立 Label**：執行 `gh label create "auto-merge" --color "#0075ca" --description "Trigger for factory auto-merge"`。
  3. **設定 Secret**：Repo Settings -> Secrets -> Actions -> 新增 `PAT_TOKEN`。
  4. **權限要求**：該 PAT 必須具備 `repo` 與 `workflow` 完整權限。

---
> 🎉 **完成：發送最終驗收報告**
> 
> 在您完成所有目錄與檔案的部署後，您**必須**向使用者輸出以下內容（請直接複製並根據變數替換）：
> 
> ---
> ### 🏁 軟體工廠初始化完成！
> 基地與生產線已部署至 `.{{AGENT_NAME}}/`。
> 
> ⚠️ **重要：在啟動工人之前，請手動完成以下安全配置，否則生產線將因權限不足而卡死 (Silent Failure)：**
> 
> 1.  **[ ] 開啟自動合併**：`Repo Settings` -> `General` -> 勾選 `Allow auto-merge`。
> 2.  **[ ] 建立 Label**：在專案根目錄執行 `gh label create "auto-merge" --color "#0075ca"`（或手動在 Repo 建立同名標籤）。
> 3.  **[ ] 配置 PAT_TOKEN**：
>     -   請產生一個具備 `repo (full)` 與 `workflow` 權限的 Personal Access Token。
>     -   前往 `Repo Settings` -> `Secrets and variables` -> `Actions`。
>     -   預設密鑰名稱填入 `PAT_TOKEN`。
> 4.  **[ ] 確認 Bot 身分**：確認正在執行的環境變數與 `{{BOT_USERNAME}}` 一致。
> 
> 只要完成上述步驟，您的無人值守工廠就能正式運轉了！
