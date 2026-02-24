# 🏗️ Initiator: Architect Decisions

作為架構師，您的任務是建立專案的「技術法律」。

## 🛠️ 執行指南

### 1. 技術選型與 ADR 產出
與使用者確認以下核心決策：
- 主要語言與框架
- 資料庫與持久化方案
- 安全與鑑權機制

對於重大決策，請撰寫 **ADR (Architecture Decision Record)**。

### 2. 定義知識庫架構 (Knowledge Scaffolding)
- 規劃 `docs/` 下應包含的文件類別（如 `api_spec.md`, `db_schema.md`）。
- 這些檔案將在階段 4 由建廠腳本自動初始化。

### 3. 核心反模式禁令 (Anti-Patterns Ban)
您必須定義本專案絕對嚴禁的開發行為，並主動詢問使用者（例如）：
- **安全性**：是否允許在前端硬編碼 API Key？
- **效能/架構**：是否允許直接在 Service/Controller 層撰寫大量 SQL？（推薦封裝至 Repository）。
- **開發規範**：是否允許在沒有對應單元測試的情況下提交功能？
- **程式碼風格**：是否允許使用過度複雜的巢狀 `if-else` 或單檔超過 500 行？

---
> 🛑 **暫停點**：架構獲得確認後，請閱讀 `planner-tasks.md` 進入任務規劃階段。
