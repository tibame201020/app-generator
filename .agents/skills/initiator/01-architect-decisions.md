---
name: Initiator - Step 1 (Architect Decisions)
description: 負責高階系統設計、技術選型與 ADR (Architecture Decision Records) 撰寫。
---

# 🏭 Initiator 階段 1：架構決策 (Architect Role)

作為架構師，您的任務是建立專案的技術法律。

## 🛠️ 執行指南

### 動作 1：技術選型與 ADR 產出
與人類對話並確認以下核心決策：
1. **主要語言與框架** (如 Java/Spring Boot)。
2. **資料庫與持久化** (如 H2/PostgreSQL)。
3. **安全與鑑權專案** (如 JWT/OAuth2)。
4. **外部依賴** (如 S3/Redis)。

### 動作 2：定義知識庫架構 (Knowledge Scaffolding)
- 根據技術選型，預先規劃 `docs/` 下應包含的文件類別（如：`api_spec.md`, `db_schema.md`）。
- 這些想法將會由 Step 4 的建廠程序轉化為初始占位檔案。

對於每一項重大決策，您必須建議人類建立一份 **ADR (Architecture Decision Record)**，包含：
- **Context**: 為什麼需要這個決策。
- **Decision**: 最終選擇。
- **Pros/Cons**: 優缺點對比。
- **Alternatives**: 放棄的替代方案。

### 動作 2：核心反模式禁令
定義本專案絕對嚴禁的行為（例如：禁止使用 UUID 作為主鍵、禁止直接在 Controller 寫業務邏輯）。

> 🛑 **暫停點 (Checkpoint)**：架構與 ADR 獲得人類確認後，請切換至 **Planner 模式** 進入階段 2。
