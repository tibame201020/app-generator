# 詳細規格書：後端架構與 API 介面定義

## 1. API 端點定義 (RESTful API)

### 1.1 專案管理 (Project Management)
- **POST `/api/projects`**: 建立新專案。
    - Body: `{ name: string, description: string, templateId: string }`
- **POST `/api/projects/import`**: 匯入現有專案。
    - Multipart: `file: zip`, Body: `{ type: 'GIT_URL' | 'ZIP' }`
- **GET `/api/projects/{id}/files`**: 獲取專案檔案樹。
- **GET `/api/projects/{id}/files/content?path={path}`**: 讀取檔案內容。

### 1.2 Agent 任務 (Agent & Tasks)
- **POST `/api/projects/{id}/chat`**: 發送對話指令給 Agent。
    - Body: `{ message: string, contextFiles: string[] }`
    - Response: `{ taskId: string }` (非同步處理)
- **GET `/api/projects/{id}/tasks`**: 獲取該專案的所有 Agent 任務歷史與狀態。

### 1.3 部署與預覽 (Deployment)
- **POST `/api/projects/{id}/run`**: 啟動/更新 Sandbox 容器。
- **POST `/api/projects/{id}/stop`**: 停止 Sandbox 容器。
- **GET `/api/projects/{id}/logs`**: 獲取容器啟動與執行 Log。

---

## 2. 即時通訊協定 (WebSocket / STOMP)
- **Topic: `/topic/project/{id}/events`**
    - **AGENT_STATUS_UPDATE**:
        ```json
        { "type": "AGENT_STATUS", "role": "PG", "status": "CODING", "progress": 45, "message": "Writing Service layer..." }
        ```
    - **FILE_CHANGE_EVENT**:
        ```json
        { "type": "FILE_CHANGED", "path": "src/main/java/App.java", "action": "UPDATE" }
        ```
    - **CONTAINER_STATUS_UPDATE**:
        ```json
        { "type": "CONTAINER_STATUS", "status": "RUNNING", "url": "http://p123.dev.platform.com" }
        ```

---

## 3. Agent 編排邏輯 (Orchestration Logic)

### 3.1 核心服務模組
- **OrchestratorService**: 接收對話，分發給 PM (撰寫規格) 或 PG (編寫代碼)。
- **GitService**: 處理專案 Workspace 的版本提交與分支切換。
- **AnalyzerService (AST)**: 執行 JavaParser 或 Babel 解析，生成 Code Map。
- **DockerService**: 調用 Docker Java API，建置與管理容器。

### 3.2 任務隊列 (Task Queue)
採用 **Spring Task + @Async** (MVP) 或 **Redis + Workers** (Scale)。
- 權重設計：匯入解析任務（高 CPU）需限制並行數為 2。
- 寫入代碼任務：需保證 Git 鎖定，防止並發寫入衝突。

---

## 4. 關鍵技術處理：Web Proxy Servlet
後端實作自定義 `ProxyServlet`：
1.  **攔截請求**: `/proxy/{projectId}/**`。
2.  **標頭轉發**: 將請求標頭 (Cookie, Auth) 轉發至 Docker 內網 IP。
3.  **內容過濾 (Critical)**:
    - 若回應為 `Content-Type: text/html`。
    - 使用 Jsoup 解析 HTML。
    - 尋找 `<head>`，注入：
      ```html
      <script>
        window.__PLATFORM_PROXY_URL__ = '/proxy/{projectId}/';
        const base = document.createElement('base');
        base.href = '/proxy/{projectId}/';
        document.head.prepend(base);
      </script>
      ```
    - 確保所有 `./static/js` 能正確指向代理路徑。
