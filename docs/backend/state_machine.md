# Agent 狀態機架構指引

> **⚠️ 目前狀態：草稿 / 待實作 (Placeholder)**
> 本文件為知識庫骨架。當進入 **Phase 2 (Core State Machine & Domain Entities)** 開發時，負責的 Agent (SA/PG) **必須**在此文件中補齊以下實作細節，才能提交 PR。

## 預期應包含的文件內容
1. **Agent 角色與職責定義**：PM, UIUX, SA, PG 分別負責的 State 階段與進入/退出條件。
2. **核心狀態轉移圖**：使用 Mermaid 語法繪製 State Machine 的流轉圖。
3. **資料表 Schema 設計**：紀錄對應 Project 與 Conversation 的資料庫結構關聯。
4. **Spring State Machine 實作說明**：若是使用自定義狀態機或框架，需說明如何攔截事件 (Events) 與觸發 Guard 條件。

---
*Agent 注意事項：在進行對應模組的開發時，請將本檔案的內容替換為正式的手冊內容。*
