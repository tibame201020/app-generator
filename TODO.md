# App Generator 開發任務清單 (TODO List)

## 第一階段：研發基礎與資料持久層 (Phase 1: Foundation & Persistence)
- [x] 初始化專案結構 (Backend, Frontend, Docker)
- [x] 配置 PostgreSQL 與 Traefik 基礎設施
- [x] 實作資料庫實體 (Entities) & Repositories:
    - [x] `User` & `Project` 表
    - [x] `AgentTask` 表 (用於存儲 CoT)
    - [x] `ContainerInstance` 表 (狀態追蹤)
- [x] 實作 `GitService` 基礎:
    - [x] 初始化 `/data/repos` 目錄
    - [x] 實現建立 Bare Git Repository 的 API
    - [x] 實現 Git Commit & Push 邏輯 (用於代碼保存)

## 第二階段：後端核心模組與沙盒管理 (Phase 2: Core Backend & Sandbox)
- [x] 實作 `DockerService`:
    - [x] 配置 Docker Java Client 連線
    - [x] 容器啟動邏輯 (指定 CPU/RAM Quota)
    - [x] 帶有 Traefik Label 的動態容器路由生成
- [x] 實作 `ProxyServlet`:
    - [x] 請求攔截與 Header 轉發
    - [x] **HTML 注入**: 自動插入 `<base>` 標籤修正路徑
- [x] 實作 `ReaperTask`:
    - [x] 定期檢查並清除 15 分鐘無活動的容器

## 第三階段：前端介面與編輯器集成 (Phase 3: Frontend Shell & IDE)
- [x] 建立 VS Code Style Layout:
    - [x] 可伸縮之 SideBar (File Explorer)
    - [x] 可切換之 Main Tab System (Editor / Canvas)
    - [ ] Chat Panel (右側)
- [x] 整合 `Monaco Editor`:
    - [x] 檔案樹讀取邏輯
    - [x] 檔案保存連動 Git Commit
- [x] 建立 `Zustand` 全域狀態機:
    - [x] 管理當前專案、檔案、Agent 活躍狀態
- [x] 實作 Runtime Controls (Run/Stop/Restart) & Preview Pane

## 第四階段：AI 任務編排與畫布互動 (Phase 4: Agent Orchestration & Canvas)
- [x] 配置 WebSocket (STOMP):
    - [x] 後端消息推送頻道 (`/topic/project/{id}`)
    - [x] 前端訂閱並即時反應 Agent 任務狀態
- [x] 實作 `Workflow Canvas`:
    - [x] React Flow 自定義 Agent 節點 (PM, SA, PG, QA)
    - [x] Workflow 資料持久化與執行 (Backend Integration)
- [x] 整合 LLM (LangChain4j):
    - [x] 實作 `PM Agent` 功能定義 Prompt
    - [x] 實作 `PG Agent` 代碼生成與修改 Prompt (基礎版本)

## 第五階段：既有專案匯入與 AST 分析 (Phase 5: Import & AST Analysis)
- [ ] 實作 `ASTAnalyzer`:
    - [ ] Java 專案結構掃描 (JavaParser)
    - [ ] JS/TS 專案結構掃描 (Babel)
- [ ] **混合解析流程 (Hybrid Parsing)**:
    - [ ] 結構掃描 -> Canvas 生成
    - [ ] 非同步 LLM 摘要填充節點語意
- [ ] 處理 `Lazy Loading Context`:
    - [ ] 根據當前任務僅提取相關檔案餵給 AI

## 第六階段：測試與安全加固 (Phase 6: Testing & Security)
- [ ] 安全隔離測試:
    - [ ] 驗證 Sandbox 內部的資源限制
    - [ ] 驗證多租戶容器間的網路隔離
- [ ] 效能驗證:
    - [ ] 冷啟動延遲優化 (Image Layering)
    - [ ] WebSocket 高併發消息穩定性
- [ ] 最終 E2E 測試:
    - [ ] 「一句話」從 0 到部署成品完整流程
