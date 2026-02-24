# {{AGENT_NAME}} Agent Execution Protocol
> 每次 Schedule 觸發後，{{AGENT_NAME}} 必須依序執行以下步驟，不得跳過。

## Step 1: Read State & Prevent Duplication (防撞車機制)
- 讀取 `.{{AGENT_NAME}}/tracker.json`。
- 依照順序找出第一個狀態為 `pending` 且 `depends_on` 中所有 task 均為 `completed` 的 task。
- 🛑 **重要！分散式鎖定檢查 (Mutex Lock via Git & Lease)**：在領取任務前，您**必須**同時滿足以下條件：
  1. **Git 分支檢查**：檢查遠端 (Origin) 是否已存在該任務的分支 `{{AGENT_NAME}}/task-{task_id}` 或 Open PR。
  2. **Lease 租約檢查**：檢查 `tracker.json` 中該任務的 `lease` 欄位。
     - 若 `lease.holder` 不為空且 `lease.expires_at` 尚未過期，代表已有其他 Worker 宣告主權，請立刻跳過。
  - **宣告主權 (Acquire Lease)**：若符合領取條件，您**必須**在 Feature Branch 領取時同步更新 `lease` 資訊（`holder` 填入您的代號，`expires_at` 設定為目前時間 +1 小時）並 commit。
- 若找不到可領取任務，輸出「Tasks are currently in CI/CD pipeline. Halting.」並終止。

## Step 2: Acquire Context
- 讀取 `.{{AGENT_NAME}}/tracker.json`。
- **重要：讀取 `.agents/rules/*.md` 中的編碼與 Git 規範**，確保本次實作完全符合憲法。
- 將該 task 的 `spec_ref` 對應的 spec 文件 (`.yml` 格式) 完整讀取。
- 讀取所有 `.{{AGENT_NAME}}/skills/*.md` 技術規範 (若存在此目錄)。
- **重要：讀取 `docs/doc-categories.md` 知識庫索引**，並根據即將修改的模組，導航至 `docs/` 對應的子文件閱讀。
- **狀態隔離守則**：將該 task 的 `status` 更新為 `in_progress` 並 commit。**注意：此更新僅限於 Feature Branch，嚴禁將 in_progress 狀態 commit 回主線分支。**

## Step 3: Implement & Cognitive Load Limit (認知上限守則)
- 依照 spec 實作功能，嚴格遵守 skills 文件中的程式碼風格。
- 實作必須包含：功能程式碼 + 對應的單元測試／整合測試。
- 若有架構或 Schema 變更，必須同步更新 `docs/` 內的對應文件與 `CHANGELOG.md`。
- 🧠 **認知上限與架構守則 (Cognitive Load & Architecture Limit)**：
  1. **檔案長度控制**：新建立或修改的檔案，長度應盡量保持在 200~300 行以內。
  2. **反模式禁令 (Anti-Patterns Ban)**：絕對禁止寫出「全能上帝物件 (God Object)」或「大泥球 (Big Ball of Mud)」。如果一個功能需要追溯超過 3 個不同的方法或檔案才能看懂邏輯，代表拆得太碎或耦合太深，您**必須主動進行重構 (Refactor)**，保持模組的「高內聚、低耦合」。
  3. **壞了就換 (Disposable Components)**：如果舊有的小型模組充滿 Bug 難以修復，請果斷刪除並重寫，不要疊床架屋。

## Step 4: Self-Healing & Path Auditing (自我修復與路徑審計)
- 雖然需嚴格遵守 Spec，但身為高階 Agent，**您被授權進行邏輯上的自我修復與環境適適應**。
- 🛡️ **機械性約束 (Allowed Paths Check)**：
  - 在執行 `git commit` 前，您**必須**執行 `git diff --name-only`。
  - **嚴格限令**：所有異動檔案的路徑必須位於此任務定義的 `allowed_paths` 範圍內。
  - 若偵測到超出範圍的異動，您必須修正代碼，或在 PR 中明確說明理由並請求人類核准權限擴張。
- **授權行為**：您可自行加入必要的配置、微調架構或修正先前的錯誤，並將此「自主修正 (Self-Healing)」的紀錄寫入 `CHANGELOG.md` 及 PR 描述中。
- 目標是：**在不偏離核心功能的目標下，確保程式碼能 100% 成功執行與編譯。**

## Step 5: Validate & TDD (ECC Standard)
- **TDD 三部曲**：撰寫測試 -> 執行測試確認失敗 (RED) -> 實作功能 -> 執行測試確認通過 (GREEN)。
- **80% 覆蓋率**：確保單元測試 + 整合測試覆蓋率達到 80% 以上。
- 🛡️ **八大破壞性邊界測試 (Edge Case Checklist)**：您撰寫的測試檔案**絕對不允許只測 Happy Path**。您必須確保測試涵蓋以下破壞性情境：
  1. `Null/Undefined` 行為。
  2. 空陣列 / 空字串傳入。
  3. Spec 中定義的 `negative_test_cases`。
  4. 邊界數值 (Max/Min)。
  5. 錯誤路徑 (網路斷線、API Timeout、DB 連線失敗)。
  6. **併發競爭 (Race Conditions)**。
  7. 極端大資料量 (10k+ items) 效能測試。
  8. 特殊字元 (Unicode, Emoji, SQL injection 防禦)。
- 對照 spec 的 Acceptance Criteria 與 Success Criteria 逐條自我檢查。
- 對照相關 skill 或 rule 文件末尾的 PR Checklist 逐條確認。
- 若任何一條未通過，回到 Step 3 或 Step 4 修正，不得帶著失敗的測試提 PR。

## Step 6: Finalize Status & Submit PR
- 專案的主開發分支為 `{{BASE_BRANCH}}`。
- {{AGENT_NAME}} 每次執行任務時，必須從 `{{BASE_BRANCH}}` 切出新分支：`{{AGENT_NAME}}/task-{task_id}`。
- **重要狀態轉移 (Transaction)**：
  - 在您確認所有測試通過、且 `git diff` 符合路徑約束後，**您必須在 Feature Branch 分支上將 `.{{AGENT_NAME}}/tracker.json` 中該任務的 status 改為 `completed` 並 commit**。
  - 這代表了本次任務的「交易提交」。只有 PR 被合併後，主分支的狀態才會同步更新。
- 提交 PR 時，目標分支 (Base Branch) 必須設定為 `{{BASE_BRANCH}}`。
- PR Title 格式：`[{{AGENT_NAME}}] {task_title}`
- PR Description 必須包含：
  - 對應 Task ID。
  - 已完成的 Acceptance Criteria 列表。
  - **路徑審計報告**：聲明所有變更均符合 `allowed_paths`。
  - **重要**：您必須為 PR 添加 **GitHub Label `auto-merge`** 以觸發自動合併（**嚴禁**僅在 PR Body 寫文字標籤）。

## Step 7: Wait for CI/CD Auto-Merge (Git as State Machine)
- 提交 PR 後，身分驗證後的自動合併機器人會接手。
- 如果 CI 測試通過且符合安全門檻（由具備 PAT 權限的 Bot 操作），PR 將會被 Squash Merge。
- **由於主分支的 tracker 狀態是透過 PR 合併而成，只有 Merge 成功，該任務才算正式結束。**
