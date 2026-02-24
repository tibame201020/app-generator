# Jules Agent Execution Protocol
> 每次 Schedule 觸發後，Jules 必須依序執行以下步驟，不得跳過。

## Step 1: Read State
- 讀取 `.jules/tracker.json`
- 找出第一個狀態為 `pending` 且 `depends_on` 中所有 task 均為 `completed` 的 task。
- 若找不到符合條件的 task，輸出 log「No actionable task found. Halting.」並終止。

## Step 2: Acquire Context
- 將該 task 的 `spec_ref` 對應的 spec 文件 (`.yml` 格式) 完整讀取。
- 讀取所有 `.jules/skills/*.md` 技術規範。
- **重要：讀取 `docs/doc-categories.md` 知識庫索引**，並根據即將修改的模組，導航至 `docs/` 對應的子文件閱讀。
- 將該 task 的 `status` 更新為 `in_progress` 並 commit。

## Step 3: Implement
- 依照 spec 實作功能，嚴格遵守 skills 文件中的程式碼風格。
- 實作必須包含：功能程式碼 + 對應的單元測試／整合測試。
- 若有架構或 Schema 變更，必須同步更新 `docs/` 內的對應文件與 `CHANGELOG.md`。

## Step 4: Self-Healing & Autonomy (自我修復與自治)
- 雖然需嚴格遵守 Spec，但身為高階 Agent，**您被授權進行邏輯上的自我修復與環境適應**。
- 若遇到未列於 Spec 但為達成功能**絕對必要**的缺失（例如：框架衝突、缺少依賴套件、環境變數遺漏、或是前置任務邏輯導致編譯失敗）。
- **授權行為**：您可自行加入必要的配置、微調架構或修正先前的錯誤，並將此「自主修正 (Self-Healing)」的紀錄寫入 `CHANGELOG.md` 及 PR 描述中。
- 目標是：**在不偏離核心功能的目標下，確保程式碼能 100% 成功執行與編譯。**

## Step 5: Validate
- 執行所有測試（後端 `mvn test`，前端 TypeScript 檢查與 Lint），確認全數通過。
- 對照 spec 的 Acceptance Criteria 逐條自我檢查。
- 對照相關 skill 文件末尾的 PR Checklist 逐條確認。
- 若任何一條未通過，回到 Step 3 或 Step 4 修正，不得帶著失敗的測試提 PR。

## Step 6: Submit PR
- 專案的主開發分支為 `feature/jules-factory`。
- Jules 每次執行任務時，必須從 `feature/jules-factory` 切出新分支：`jules/task-{task_id}`。
- 提交 PR 時，目標分支 (Base Branch) 必須設定為 `feature/jules-factory`。
- PR Title 格式：`[Jules] {task_title}`
- PR Description 必須包含：
  - 對應 Task ID。
  - 已完成的 Acceptance Criteria 列表（逐條勾選）。
  - 測試覆蓋摘要。
  - 所影響的文件或 `CHANGELOG.md` 變更說明。
  - **必須在結尾標註 `[auto-merge]` 標籤**，以便觸發 GitHub Actions 的自動合併機制。

## Step 7: Update State
- 將 `.jules/tracker.json` 中該 task 的 `status` 更新為 `completed`。
- 將此變更包含在同一個 PR 內（不另開 PR）。
