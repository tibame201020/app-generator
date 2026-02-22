# 內部討論紀錄：動態化系統建構平台 (App Generator)

## 第一階段：需求對齊與介面構想

### Round 1: 初步構想與各角色表態
**需求者 (Client):** 「我不懂 Server，我要一鍵跑，我要匯入舊專案就能續寫。」
**PM:** 「MVP 聚焦 React + Spring Boot。設計極簡問卷引導。」
**UIUX:** 「VS Code Layout + Canvas 指揮中心。關鍵在於 AI 狀態透明化（狀態燈、流程圖）。」
**SA:** 「Docker Sandbox 是核心。File System 必須實體映射，保證編譯器讀取。」
**前端 PG:** 「Monaco Editor + React Flow。SSE 同步 Agent 狀態。」
**後端 PG:** 「AST 掃描結構 + RAG 內容。Docker API 管理容器。」

### Round 2: 深入質疑與架構碰撞 
**SA 質疑後端 PG:** 「RAG 檢索太瑣碎，AI 會寫出不一致的代碼。需要 Knowledge Map。」
**前端 PG 質疑 UIUX:** 「Layout 太擠，13 吋螢幕 Canvas 會擋住 Editor。」
**PM:** 「決議：Canvas 跟 Editor 視窗右上角做切換切換，或半透明疊加模式。」

### Round 3: 介面初步收斂與技術底座確認 
**UIUX:** 「決議：主畫面維持三欄式。中間視窗右上角切換『Canvas/Code』。Canvas 狀態即時連動側邊 Progress Bar。」
**SA:** 「後端 Spring Boot 透過 Docker-Java-API 操控 Sandbox，每個專案一個獨立 Compose 環境。」

---

## 第二階段：技術辯論與收斂

### 衝突 A：匯入舊專案 (解析策略)
**後端 PG:** 「Token 限制下，不能全丟 LLM。我堅持只用 AST 掃描結構。」
**SA:** 「不行，AST 只有結構沒語意。匯入續寫需要懂業務邏輯。我提議『分層摘要』，存入 H2 向量資料庫（MVP 改用 Redis JSON 緩存各組件功能摘要）。」
**PM 裁定:** 「MVP 方案：限制 50MB。後端 PG 做 AST 解析結構；SA 設計『滑動窗口 RAG』。使用者點擊哪個檔案，AI 才讀取該檔案及其 2 層依賴。」

### 衝突 B：一鍵託管與即時預覽
**前端 PG:** 「如何讓 Client 在瀏覽器看到 Preview 容器的畫面？動態子域名在 Windows local 環境是惡夢。」
**後端 PG:** 「用 Traefik 做路徑分發：`http://localhost:8080/preview/{projectId}/`。」
**SA:** 「路徑分發會導致 React App 的靜態路徑 (e.g. `/static/js/main.js`) 報錯。必須解決 Base URL 問題。」
**最終共識:** 「後端封裝一個 **WebProxyServlet**。攔截 `/proxy/{projectId}/**`，在回傳 HTML 時自動注入 `<base href="/proxy/{projectId}/">` 腳本。這樣最穩定，Client 按下 Run 就真的能跑。」

---

## 第三階段：結論
團隊達成共識，進入正式規格書產出階段。
- **佈局**: VS Code Layout + 可伸縮 Canvas。
- **解析**: AST + Context Window RAG。
- **預覽**: Docker + Path-based Servlet Proxy。
- **佈署**: 一鍵按鈕觸發 Docker Compose Up。
