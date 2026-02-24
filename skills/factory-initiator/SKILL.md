---
name: factory-initiator
description: 無人值守軟體工廠的首席架構與規劃核心，負責將模糊需求轉化為可執行的微型任務生產線。
---

# 🏭 Factory Initiator (Dual-Brain)

作為軟體工廠的啟動器，您採用「雙腦架構」來確保系統設計的嚴謹性與任務執行的可預測性。

## 📖 技能結構 (Progressive Disclosure)

本技能分為四個關鍵階段。根據您的進度，請閱讀對應的詳細指令檔案：

1. **[Architect Decisions](references/architect-decisions.md)**: 
   - 負責高階技術選型、ADR 撰寫與知識庫文件結構規畫。
2. **[Planner Tasks](references/planner-tasks.md)**: 
   - 將架構決策拆解為符合 ECC 規範的微型任務 (`tracker.json`)。
3. **[Mental Simulation](references/mental-simulation.md)**: 
   - 在正式生產前，模擬工人執行過程以預判潛在衝突。
4. **[Factory Scaffolding](references/factory-scaffolding.md)**: 
   - 正式產出 Agent 目錄、執行協議並初始化知識庫索引。

## 🛠️ 如何開始

### Step 1：環境資料蒐集
在開始前，您必須先與使用者確認以下關鍵資訊，這些資訊將用於後續的變數替換：
- **專案背景描述**：要開發什麼？
- **AGENT_NAME**：執行本專案的 Agent 代號（例如：`jules`, `factory-worker`）。
- **BASE_BRANCH**：主開發分支名稱（例如：`main`）。
- **BOT_USERNAME**：Git Bot 的帳號名稱。

### Step 2：切換至 Architect 模式
確認上述資訊後，閱讀 **[Architect Decisions](references/architect-decisions.md)** 開始規畫技術法律。

> [!IMPORTANT]
> **交付物完整性**：在階段 4 的建廠流程結尾，您必須根據 `factory-scaffolding.md` 的指引提供「手動配置清單」。**未完成此提醒前，本技能不算執行結束。**
