# 技術決策與差異紀錄 (Decision Log)

本文件詳述「原始版本」與「多 Agent 集成增強版」之間的重大決策轉變與技術補強。

## 1. 檔案存儲策略
- **原始方案**: 實體檔案映射 (Standard FS Mapping)。
- **增強方案**: **Git-Based Storage (Bare Repo)**。
- **決策理由**: 為了支持「匯入舊專案續寫」，必須具備完善的版本控制與還原能力。使用 Git 能讓 AI 在不同分支試錯，且方便未來擴展多角色共同編輯。

## 2. 代碼解析與理解 (Import 策略)
- **原始方案**: 基礎 RAG + LLM 全量讀取。
- **增強方案**: **Hybrid Parsing (AST 骨架 + LLM 漸進式摘要)**。
- **決策理由**: 解決 Token 爆炸與延遲問題。使用者上傳後「秒看結構」，背景任務再「點亮細節」，大幅提升產品流暢感。

## 3. 網路預覽架構
- **原始方案**: Web Proxy Servlet (路徑轉發)。
- **增強方案**: **Dynamic Reverse Proxy (Traefik + Subdomain)**。
- **決策理由**: Subdomain 方案能徹底解決 React/Spring Boot 應用的靜態資源路徑 (Base URL) 問題，讓預覽環境與生產環境配置 100% 一致。

## 4. AI 互動與透明度
- **原始方案**: 簡單 SSE 狀態推送。
- **增強方案**: **WebSocket (STOMP) + CoT (Chain of Thought) 儲存**。
- **決策理由**: 為了滿足 Client 對「透明化」的極度渴求，系統將 AI 的每一步思考 (Log) 寫入 `AgentTasks` 表並即時透過 Canvas 展示節點脈衝動畫。

## 5. 資料庫規劃
- **原始方案**: H2 為主。
- **增強方案**: **PostgreSQL (系統核心) + H2 (用戶 Sandbox 預設)**。
- **決策理由**: 保證平台自身的數據安全性與擴展性，同時保留用戶 App 的輕量化啟動優勢。
