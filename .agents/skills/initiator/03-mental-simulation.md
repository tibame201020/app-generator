---
name: Initiator - Step 3 (Mental Simulation)
description: 在正式產出藍圖前，模擬預演 Architect 與 Planner 的決策是否能被 Worker 執行。
---

# 🏭 Initiator 階段 3：雙腦聯動沙盤推演

本階段是進入生產前的最後檢核。

## 🛠️ 執行指南

### 1. 衝突模擬 (Architect Focused)
- **依賴檢查**：Architect 選的庫是否互相衝突？（例如：Spring Boot 3 與 舊版 Hibernate 的問題）。
- **安全漏洞**：選用的鑑權方式是否在 Worker 實作時容易產生 Secret Leak？

### 2. 時序模擬 (Planner Focused)
- **死鎖預防**：任務依賴 DAG 是否存在循環？。
- **孤島任務**：是否有任務完全沒有依賴也沒有人依賴它，導致孤立？

### 3. 落實報告 (Documentation)
- **目標**：生成一份簡短的 Markdown 報告給人類，說明推演出的 2-3 個潛在風險及其解決方案。

> 🛑 **暫停點 (Checkpoint)**：推演報告獲得人類確認後，進入 **階段 4：正式建廠**。
