# 詳細規格書：Sandbox 安全隔離與部署策略

## 1. 容器隔離技術 (Docker Sandbox)

### 1.1 執行時限制 (Runtime Constraint)
每個專案容器啟動時必須強制附加以下限制，防止惡意代碼影響宿主機：
- **CPU**: `--cpus=0.5` (限制最高使用 0.5 核)。
- **Memory**: `-m 512m --memory-swap 512m` (禁止使用 Swap，防止 OOM 導致宿主機卡死)。
- **User**: `--user 1000:1000` (禁止以 root 權限執行應用)。
- **Storage**: `--storage-opt size=1G` (限制容器寫入層大小)。
- **PID Limit**: `--pids-limit 100` (防止 Fork Bomb)。

### 1.2 網路安全 (Network Policies)
- **獨立 Bridge**: 每個專案自定義一個 `project_{id}_net`。
- **出口限制**: 使用 `iptables` 或 `Docker Desktop` 設定，僅允許訪問必要的軟體庫代鑰端點（如 Maven Central, NPM Registry），禁止訪問其他專案容器。

---

## 2. 動態路由與反向代理 (Traefik)

### 2.1 Traefik 標籤模板 (Labels)
當 `DockerService` 啟動容器時，需自動注入以下 Label：
```yaml
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.{projectId}.rule=Host(`{projectId}.dev.platform.com`)"
  - "traefik.http.services.{projectId}.loadbalancer.server.port=8080"
  - "traefik.http.routers.{projectId}.entrypoints=web"
```

### 2.2 預覽超時與資源回收 (Reaper)
- **閒置檢測**: 後端 `ReaperTask` 每分鐘查詢 `container_instances` 表。
- **邏輯**: 若 `last_active_at` 超過 15 分鐘，執行：
    1.  `docker stop {id}`
    2.  `docker rm {id}`
    3.  更新資料庫狀態為 `STOPPED`。

---

## 3. DevOps 自動化流

### 3.1 映像檔建置 (Image Builder)
為了加速啟動，採用 **層級式映像檔 (Layered Images)**：
- **Base**: `ubuntu:22.04` + `JDK/Node` 生態環境。
- **Cached**: 已預熱好常用依賴（如 Spring Boot Starter 快取）的基礎鏡像。
- **Final**: 僅將 `/data/workspaces/{sid}` 掛載為 Volume，無需每次進行 `docker build`，改用 `docker run` 搭配快速啟動腳本。

---

## 4. 監控與告警
- **Prometheus 整合**: 收集各容器的 `container_cpu_usage_seconds_total`。
- **自動 Kill**: 若單一專案內存佔用連續 30 秒超過 90%，系統自動重啟該容器並推送通知給使用者。
