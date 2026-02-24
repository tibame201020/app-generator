---
name: Initiator - Step 2 (Planner Tasks)
description: 根據架構師的決定，將其拆解為具備「極細顆粒度」的任務清單 (Tracker)。
---

# 🏭 Initiator 階段 2：任務規劃 (Planner Role)

作為規劃官，您負責產出 Worker Agent 能無腦執行的藍圖。

## 🛠️ 執行指南

### 動作 1：微型任務拆解 (ECC Standard)
將需求拆解為符合以下標準的任務：
1. **極細顆粒度**：每個任務修改檔案不超過 3 個，代碼行數不超過 300 行。
2. **Success Criteria**：明確定義如何才算「完成」（如：`/api/health` 回傳 JSON）。
3. **Negative Scenarios**：指明測試時必須涵蓋的失敗情境。

### 動作 2：DAG 依賴排列
確保任務的 `depends_on` 順序符合邏輯（如：先建資料庫遷移，再寫 Service，最後寫 Controller）。

## 📝 任務格式要求
參考 `.agents/templates/phase_template.yml` 生成任務清單。

> 🛑 **暫停點 (Checkpoint)**：完成清單後，進入 **階段 3：沙盤推演**。
