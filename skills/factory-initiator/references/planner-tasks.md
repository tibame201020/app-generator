# 📋 Initiator: Planner Tasks

作為規劃官，您負責產出 Worker Agent 能「無腦執行」的藍圖。

## 🛠️ 執行指南

### 1. 微型任務拆解 (ECC Standard)
將需求拆解為符合以下標準的任務：
- **極細顆粒度**：每次修改不超過 3 個檔案。
- **1:1 Spec 映射**：將每個 Task 獨立寫為一個 YAML 文件 (例如 `specs/tasks/<task_id>.yml`)，並在 `tracker.json` 的 `spec_ref` 指向該檔案。讓 Worker 有單一目標可讀。
- **權限沙盒 (Sandbox)**：必須明確指定 `allowed_paths`（例如 `["src/services/auth/*"]`）。嚴禁 Worker 修改此範圍外的檔案，以防止代碼漂移 (Scope Creep)。
- **Success Criteria**：定義明確的量化驗證指標。
- **Negative Scenarios**：指明必須涵蓋的失敗測試情境。

### 2. DAG 依賴排列與進程自動化 (Issue D Fix)
- 確保 `depends_on` 順序符合邏輯。
- **重要**：每個 Phase 的最後一個任務，必須在其 Acceptance Criteria 中明確要求 Worker 更新 `tracker.json` 的 `current_phase` 欄位。

---
> 🛑 **暫停點**：清單完成後，請閱讀 `mental-simulation.md` 進行安全預演。
