# 詳細規格書：資料庫 Schema 與儲存策略

## 1. 資料庫設計 (PostgreSQL)

### 1.1 使用者與安全
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    plan_type VARCHAR(50) DEFAULT 'FREE', -- FREE, PRO, ENTERPRISE
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 1.2 專案與存儲
```sql
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    git_repo_path TEXT NOT NULL, -- 伺服器上的 Bare Repo 路徑
    tech_stack VARCHAR(50), -- e.g., 'SPRING_BOOT_REACT'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 1.3 Agent 任務記錄 (思考鏈)
```sql
CREATE TABLE agent_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    agent_type VARCHAR(50) NOT NULL, -- PM, SA, PG
    task_name VARCHAR(255), -- e.g., 'GENERATE_SCHEMA'
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, RUNNING, SUCCESS, FAIL
    context_data JSONB, -- 儲存當前對話上下文快照
    log_content TEXT, -- Agent 的 Chain of Thought (CoT) 原始紀錄
    progress_pct INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 1.4 容器實體追蹤
```sql
CREATE TABLE container_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    container_id VARCHAR(255), -- Docker ID
    subdomain VARCHAR(100) UNIQUE, -- e.g., 'p123.dev.platform.com'
    internal_ip VARCHAR(50), -- Docker 網間 IP
    status VARCHAR(50), -- STARTING, RUNNING, STOPPED, EXPIRED
    last_access_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## 2. 儲存策略：Git-Based Persistence

### 2.1 目錄結構 (Host Side)
```text
/data/app-generator/
├── repos/                  # 所有專案的 Bare Git Repositories
│   └── {userId}/
│       └── {projectId}.git/ 
├── workspaces/             # 活躍 session 的工作目錄 (Checkouts)
│   └── {sessionId}/
│       └── current_project/
└── backups/                # 定期專簽快照 (Zip)
```

### 2.2 寫碼與提交流程 (PG Agent Workflow)
1.  **Read**: Agent 從 `workspaces/{sessionId}` 讀取檔案。
2.  **Write**: Agent 修改檔案。
3.  **Auto-Commit**: 後端 `GitService` 自動執行：
    ```bash
    git add .
    git commit -m "AI_UPDATE: [AgentName] updated [FileName]"
    git push origin main
    ```
4.  **Broadcast**: 透過 WebSocket 通知前端 `FILE_CHANGED`。

---

## 3. 性能優化策略
- **向量快取 (Vector Cache)**:
    - 雖然不直接存儲於 PG，但在 `projects` 表中記錄緩存路徑。
    - 指向該專案的 **ChromaDB** 或 **FAISS** 索引目錄，用於 RAG 檢索。
- **冷熱數據分離**:
    - `agent_tasks` 超過 30 天的 Log 移至 `agent_tasks_history` 或冷存儲，保持主表查詢速度。
