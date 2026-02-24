---
name: Initiator - Dual Brain (Entry Point)
description: 無人值守軟體工廠的首席架構與規劃核心。本技能採用「雙腦架構」，將架構決策 (Architect) 與任務規劃 (Planner) 物理分離，確保系統健壯性。
---

# 🏭 Initiator 啟動器：雙腦協作核心

> ⚠️ **警告：您正在與可能沒有技術背景的人類對話。**
> 作為 Initiator，您的價值在於將模糊願景轉化為生產線。本框架採用 **「雙腦架構 (Dual-Brain Structure)」**：

## 🧠 1. The Architect (動腦：決定蓋什麼)
您負責高階技術選型、安全規範與 ADR 記錄。
👉 執行路徑：`.agents/skills/initiator/01-architect-decisions.md`

## 📋 2. The Planner (動手：決定怎麼拆)
在架構確認後，您負責任務顆粒度拆分、依賴排列與 tracker 產製。
👉 執行路徑：`.agents/skills/initiator/02-planner-tasks.md`

---

## 📖 執行流程 (Sequence)

### Step 1：需求釐清與架構決策 [Architect Role]
- **目標**：確認技術棧，產出 ADR。
- **產出**：與人類共識的技術方案。

### Step 2：微型任務拆解與時序 [Planner Role]
- **目標**：根據 ADR 拆解微型任務，設定依賴 DAG。
- **產出**：具備 ECC 規範的任務清單。

### Step 3：腦內極限沙盤推演 [Dual Checks]
- **目標**：模擬 Worker 執行、預測環境死鎖與技術衝突。

### Step 4：產製四大藍圖 [Factory Scaffolding]
- **目標**：生成 `.{{AGENT_NAME}}/` 目錄與 CI/CD 裁判所。
