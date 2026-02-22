# 動態化系統建構平台 - 增強版系統規格書 (Enhanced System Spec)

## 1. 系統願景與產品定位
打造一個「對話即系統」的開發平台，支援全新專案生成與既有專案匯入。透過 **AST+LLM 混合解析** 與 **Git 基底儲存**，為使用者提供透明、可控且一鍵部署的開發體驗。

---

## 2. 核心功能清單

### 2.1 專案生命週期
- **New Project**: PM Agent 引導式需求收集 -> SA Agent 自動建模 -> PG Agents 併行編碼。
- **Existing Project (特色)**: 
    - **AST 快速掃描**: 建立檔案依賴與函式引用關係。
    - **LLM 非同步摘要**: 背景任務逐一解析業務邏輯並「點亮」Canvas。
    - **Lazy Loading Context**: 僅在編輯時讀取深層代碼，優化 Token 消耗。

### 2.2 協作與編輯器
- **Workflow Canvas (React Flow)**: 
    - 顯示 Agent 節點與任務流向動畫。
    - 支援「圖轉碼」與「碼轉圖」的雙向綁定。
- **VS Code 體驗 (Monaco)**: 整合 AI 代碼補全、多工作區標籤。
- **即時終端 (xterm.js)**: 串流顯示 Docker Build 與應用啟動日誌。

### 2.3 部署與預覽
- **一鍵 Run**: 自動偵測技術棧（Spring Boot / React），掛載實體 Volume 啟動容器。
- **即時網域**: 動態分配 `{projectId}.dev.platform.com`。
- **自動休眠**: 15 分鐘無流量自動暫停容器以節省資源。

---

## 3. 系統架構設計

### 3.1 儲存策略：Git-Based File System
- **核心儲存**: 伺服器端維護 **Bare Git Repos** (`/data/projects/{uid}/{pid}.git`)。
- **優勢**: 天然支援版本回溯、分支實驗、CI/CD 整合。
- **工作區**: 編輯時 Check out 到暫存緩存，容器啟動時透過 `Volume Mount` 掛載。

### 3.2 隔離與安全：Docker Sandbox
- **Resource Quota**: 限制 0.5 CPU, 512MB RAM, 1GB Disk。
- **Network Isolation**: 容器位於獨立 `preview-net`，禁止存取 Host 內網區。
- **Security Check**: 防止 RCE 存取敏感路徑，限制寫入權限。

### 3.3 網路路由：Dynamic Reverse Proxy
- **技術選型**: **Traefik** (原生支援 Docker Provider)。
- **自動發現**: 容器啟動後，Traefik 自動偵測標籤並發布路由規則。
- **解決路徑問題**: 使用者應用程式預設運行於根路徑 `/`，搭配動態子網域解決靜態資源引用問題。

---

## 4. AI 任務處理模型 (Hybrid Logic)
1. **同步層 (AST)**: 快速產出結構，「幾秒內」讓使用者看到初步圖表。
2. **非同步層 (LLM)**: 使用 RabbitMQ 派發摘要與重構任務，避免阻塞主線程。
3. **通知系統 (WebSocket)**: Agent 的思考鏈 (CoT) 與進度透過 WebSocket 即時推送至 Canvas。

---

## 5. 資料庫設計 (PostgreSQL)

| 表名 | 關鍵欄位 | 備註 |
| :--- | :--- | :--- |
| **Users** | id, username, plan_type | |
| **Projects** | id, userId, name, git_repo_path | |
| **AgentTasks** | id, projectId, status, log_json | 存儲 CoT 思考過程 |
| **ContainerInstances** | id, projectId, containerId, last_active_at | 用戶容器生命週期追蹤 |
| **VectorCache (Optional)** | id, filePath, summary_vector | 用於 RAG 快速檢索 |

---

## 6. 技術選型表
| 領域 | 技術選型 | 理由 |
| :--- | :--- | :--- |
| **前端** | React 18, React Flow, Monaco | 頂級開發者體驗。 |
| **後端** | Spring Boot 3, LangChain4j | 企業級穩定性，AI 整合力強。 |
| **代理** | Traefik | 動態路由自動化首選。 |
| **儲存** | Bare Git, PostgreSQL | 版本控制與結構化數據並重。 |
| **任務佇列** | Spring Task (MVP) / Redis (Prod) | 處理非同步 AI 分析任務。 |

---

## 7. 驗證與 PoC 計畫
1. **解析 PoC**: 匯入一個 5 萬行的專案，測試 AST 掃描延遲與 Canvas 呈現流動感。
2. **啟動 PoC**: 驗證 Docker 冷啟動至 Traefik 路由生效的總時長（目標 < 5s）。
3. **壓力測試**: 模擬 20 個併發 Agent 寫碼任務，觀察後端執行緒池狀態。
