# 01 核心概念與狀態機 (Core Concepts)

## 為什麼不用資料庫？(Git as a State Machine)

傳統的自動化工作流工具（如 n8n 或 Airflow）高度依賴一個中央資料庫來記錄「哪個任務走到哪一步了」。
但在無人值守軟體工廠中，如果我們引入外部資料庫，會面臨一個致命問題：**「狀態與程式碼不同步 (State Drift)」**。

想像一下：AI 工人回報資料庫「我已經把登入功能做好了 (Task Completed)」，但他的代碼其實有 Bug，根本沒有進到主分支。這會導致下一個依賴登入功能的任務啟動時大爆炸。

### 唯一的真理：Merge 才是真理 (Single Source of Truth)

我們徹底拋棄了外部資料庫，將狀態機寫死在程式碼的根目錄：`.{{AGENT_NAME}}/tracker.json` 中。

**流轉機制：**
1. **[領取]** AI 工人切換出新分支 (Branch)。
2. **[宣稱完工]** AI 寫完程式，在**該分支**將 tracker.json 中的狀態改為 `"completed"` 並提交 PR。
3. **[仲裁]** 全自動的 CI/CD 擔任無情的裁判。
   - 如果測試失敗，PR 不會被合併。**主分支 (Main) 的狀態永遠停在 `"pending"`**。
   - 如果測試成功，PR 被 Squash Merge。**主分支的狀態才正式且合法地轉變為 `"completed"`**。

這形成了一個完美的 **無伺服器防呆機制** 以及 **自動退回 (Rollback) 與重試 (Retry) 機制**。

---

## 雙軌制角色 (Dual-Track Agent Architecture)

這座工廠中，AI 被嚴格地分為兩種職位，**這兩種職位絕對不可以互換，也絕對不能混淆大腦**。

### 1. The Initiator (啟動器 - 雙腦架構)
- **職責**：軟體工廠的戰略中心。採用 **「雙腦分離 (Dual-Brain)」**，將「架構決策 (Architect)」與「任務拆解 (Planner)」邏輯物理隔離。
- **產出物**：`tracker.json` (帶有 `allowed_paths` 約束)、`specs/*.yml` (Spec 2.0 規格) 與 ADR 紀錄。
- **生命週期**：一次性啟動，完成建廠與知識庫初始化後即下線。

### 2. The Worker (工人)
- **職責**：不准做大決策，只准乖乖照著 Specs 寫程式。
- **產出物**：真正的業務程式碼 (Frontend/Backend) 與 自動化測試。
- **生命週期**：藉由 Cronjob 排程或是 Webhook 被無限次喚醒，直到所有的 `pending` 任務都被清除。

當大腦 (Initiator) 與手腳 (Worker) 被物理隔離後，LLM 最容易發生的「無效重構」與「幻覺迷失」將被徹底杜絕。
