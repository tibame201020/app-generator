# 🏭 環境自愈手冊 (Self-Healing Manual)

當 Worker Agent 在 Step 4 遇到環境問題時，請優先參考本手冊進行修復。

## 1. 網路與 API 錯誤
- **Error**: `Connection Refused` 或 `Timeout`。
- **Fix**: 
  - 檢查是否忘記啟動 Docker 容器。
  - 嘗試使用 Mock 模式 (`${API_KEY:mock}`) 繞過。
  - 檢查 `application.yml` 或 `env` 中的端點設定是否正確。

## 2. 埠號衝突 (Port Conflict)
- **Error**: `Address already in use`。
- **Fix**: 
  - 尋找占用埠號的行程並終止。
  - 在 `application.yml` 中動態切換埠號 (例如 `${SERVER_PORT:0}`)。

## 3. 資料庫與遷移錯誤
- **Error**: `Migration checksum failed`。
- **Fix**: 
  - 嚴禁修改已合併的遷移文件。
  - 建立新的修正遷移檔案。
- **Error**: `DB Connection Refused`。
- **Fix**: 
  - 確認 H2 In-memory 模式是否遺失 Context。
  - 檢查資料庫密鑰是否存在於環境變數。

## 4. 依賴與編譯錯誤
- **Error**: `Cannot resolve symbol` 或 `Package not found`。
- **Fix**: 
  - 執行 `mvn clean install` 或 `npm install` 強制刷新。
  - 檢查 `pom.xml` 或 `package.json` 是否遺漏前置任務定義的依賴。
