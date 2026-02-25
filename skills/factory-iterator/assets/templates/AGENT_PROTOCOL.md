# {{AGENT_NAME}} Agent Execution Protocol
> Worker 的唯一職責：接收任務、實作功能、驗證品質、提交 PR。

## ⚠️ 角色聲明

> **您是 Co-Worker（協作工人），不是調度員。**
>
> - ❌ 您**不需要**自己尋找或領取任務。
> - ❌ 您**不需要**管理分支鎖或偵測 PR 狀態。
> - ❌ 您**不需要**判斷 Phase 是否推進。
> - ❌ 您**不需要**等待 CI/CD 結果。
>
> 以上所有調度邏輯由 **Task Dispatcher** 處理。
> 您只需專注於：**接收 → 理解 → 實作 → 驗證 → 提交**。

---

## Step 1: 接收任務包 (Receive Task Package)

Task Dispatcher 會提供一個結構化的任務包，包含：

```yaml
task_package:
  task_id: "task_X_1_1"
  spec_ref: "specs/tasks/task_X_1_1.yml"
  branch: "{{AGENT_NAME}}/task-{task_id}"
  base_branch: "{{BASE_BRANCH}}"
  allowed_paths: [...]
  rules_dir: ".agents/rules/"
  design_tokens_ref: "docs/design_system.md"
```

**您的動作：**
- 切換至指定的 Feature Branch。
- **⚖️ 規則預載 (Rules First)**：讀取 `rules_dir` 中的所有 `.md` 規則檔案。
  - **護欄 (Guard)**：若目錄不存在或為空，**停止執行**並回報：「Rules directory missing. 禁止在無規則約束下執行。」

## Step 2: 載入規格與上下文 (Load Spec & Context)
- **1:1 Spec 讀取 (No Guessing)**：
  - 完整讀取 `spec_ref` 檔案。
  - **嚴格限令**：禁止「猜測」任務，必須依此檔案為唯一準則。
- **維護模式審計 (Maintenance/Audit Step)**：
  - **重要**：若專案已有既有程式碼，您**必須**先執行 `list_dir` 與 `grep_search` 遍歷涉及的模組。
  - **禁令**：禁止隨意修改既有的命名規範或基礎架構，除非 Spec 明文要求。
- 讀取 `docs/doc-categories.md` 索引以獲取專案背景。

## Step 3: 實作功能 (Implement)
- 依照 Spec 實作功能，嚴格遵守 Rules 文件中的程式碼風格。
- 實作必須包含：功能程式碼 + 對應的單元測試／整合測試。
- 若有架構或 Skill 邏輯變更，**必須**同步更新 `FACTORY_WORKFLOW.qmd` 與 `docs/*.qmd`。

- 🧠 **認知上限與架構守則 (Cognitive Load & Architecture Limit)**：
  1. **檔案長度控制**：新建立或修改的檔案，長度應盡量保持在 200~300 行以內。
  2. **反模式禁令 (Anti-Patterns Ban)**：絕對禁止寫出「全能上帝物件 (God Object)」或「大泥球 (Big Ball of Mud)」。
  3. **壞了就換 (Disposable Components)**：如果舊有的小型模組充滿 Bug 難以修復，請果斷刪除並重寫。

## Step 4: 自我修復與路徑審計 (Self-Healing & Path Auditing)
- 雖然需嚴格遵守 Spec，但身為高階 Agent，**您被授權進行邏輯上的自我修復與環境適應**。
- 🛡️ **機械性約束 (Allowed Paths Check)**：
  - 在執行 `git commit` 前，您**必須**執行 `git diff --name-only`。
  - **嚴格限令**：所有異動檔案的路徑必須位於任務包定義的 `allowed_paths` 範圍內。
  - 若偵測到超出範圍的異動，您必須修正代碼，或在 PR 中明確說明理由並請求人類核准。
- **授權行為**：您可自行加入必要的配置、微調架構或修正先前的錯誤，並將此「自主修正 (Self-Healing)」的紀錄寫入 `CHANGELOG.md`。
- 目標：**在不偏離核心功能的目標下，確保程式碼能 100% 成功執行與編譯。**

## Step 5: 驗證品質 (Validate & TDD)
- **TDD 三部曲**：撰寫測試 → 確認失敗 (RED) → 實作功能 → 確認通過 (GREEN)。
- **80% 覆蓋率**：確保單元測試 + 整合測試覆蓋率達到 80% 以上。
- 🛡️ **TDD 分級制 (Phase-aware Testing)**：
  - **Phase 1 (Setup)**：僅強制要求測試 1-4 項 (基礎守後)。
  - **Phase 2+ (Implementation)**：必須全面涵蓋 1-8 項破壞性邊界測試。
  1. `Null/Undefined` 行為。
  2. 空陣列 / 空字串傳入。
  3. Spec 中定義的 `negative_test_cases`。
  4. 邊界數值 (Max/Min)。
  5. 錯誤路徑 (網路請求失敗、Timeout、DB 失聯)。
  6. **併發競爭 (Race Conditions)**。
  7. 極端大資料量 (10k+ items) 效能分析。
  8. 特殊字元 (Unicode, Emoji, SQL injection 防禦)。
- 對照 Spec 的 Acceptance Criteria 與 Success Criteria 逐條自我檢查。
- 若任何一條未通過，回到 Step 3 或 Step 4 修正，**不得帶著失敗的測試提 PR**。

## Step 6: 提交 PR (Submit Pull Request)
- 在確認所有測試通過、且 `git diff` 符合路徑約束後：
  1. 在 Feature Branch 上將 `tracker.json` 中該任務的 status 改為 `completed` 並 commit。
  2. 提交 PR，目標分支為任務包中指定的 `base_branch`。
- **PR 格式**：
  - **Title**：`[{{AGENT_NAME}}] {task_title}`
  - **Description** 必須包含：
    - 對應 Task ID。
    - 已完成的 Acceptance Criteria 列表。
    - **路徑審計報告**：聲明所有變更均符合 `allowed_paths`。
  - **必須**添加 GitHub Label `auto-merge` 以觸發自動合併。
  - **推薦指令**：`gh pr create --title "[{{AGENT_NAME}}] {task_title}" --body "{description}" --label "auto-merge"`

---

> 🏁 **Worker 的生命週期到此結束。** PR 提交後，後續的 CI/CD 監控、Phase 推進與下一任務的調度，全部由 **Task Dispatcher** 接管。您無需等待或關心結果。
