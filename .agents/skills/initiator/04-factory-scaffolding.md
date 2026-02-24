---
name: Initiator - Step 4 (Factory Scaffolding)
description: 根據前述推演結果，產出 `.jules/` (或對應 Agent) 目錄與核心協議。
---

# 🏭 Initiator 階段 4：建置生產線與裁判所

## 🛠️ 執行指南

### 1. 實體化藍圖
- 讀取 `.agents/templates/tracker.json`，將 Planner 拆解的任務寫入，存為 `.{{AGENT_NAME}}/tracker.json`。
- 讀取 `.agents/templates/AGENT_PROTOCOL.md`，替換變數後存為 `.{{AGENT_NAME}}/AGENT_PROTOCOL.md`。

### 2. 初始化核心文件與知識庫
- **建立目錄**：若不存在，則建立 `docs/` 與 `docs/ADR/`。
- **初始化索引**：將 `.agents/templates/doc-categories.md` 複製至 `docs/doc-categories.md`。
- **產出占位文件**：根據 Architect 在階段 1 的規劃，在 `docs/` 下建立對應的 Markdown 空檔案（避免 Worker 讀取失敗）。

### 2. 部署規則庫
- **主動提醒**：告知人類，Worker Agent 將會讀取 `.agents/rules/` 下的規則。

### 3. CI/CD 合併裁判所
- 根據 Architect 的技術選型，修改 `.agents/templates/auto-merge.yml` 中的測試指令（如 `mvn test` 改為 `pytest`）。
- 存為 `.github/workflows/{{AGENT_NAME}}-auto-merge.yml`。

> 🎉 **恭喜 (Finish)**：建廠完成！請告知人類已可啟動生產線。
