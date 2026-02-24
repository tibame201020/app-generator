---
name: Initiator - Step 3 (Factory Scaffolding)
description: 根據前述推演結果與變數，利用本機端的架構模板與擴充套件，為專案生成完整的工廠啟動包。
---

# 🏭 Initiator 階段 3：建置自動化裁判所與工廠藍圖

在建置工廠前，**您必須先讀取先前的對話狀態，確認以下環境變數**：
- `{{AGENT_NAME}}`：該工廠將配置的 AI 工人名稱 (例如 `jules`, `dev-bot` 等)。
- `{{BASE_BRANCH}}`：專案的主要開發分支 (例如 `feature/ai-factory`, `main` 等)。
- `{{BOT_USERNAME}}`：AI 機器人在 Version Control 平台上的帳號名稱。

確認完畢後，請從 `.agents/templates/` 目錄中讀取對應的範本文件，並建立專案的四大藍圖：

## 🛠️ 執行指南

### 1. 設置狀態機與規格防爆網
- 讀取 `.agents/templates/tracker.json`，將推演出的所有任務寫入，預設狀態設為 `pending`，並存為 `.{{AGENT_NAME}}/tracker.json`。
- 建立 `specs/` 目錄，並參考 `.agents/templates/phase_template.yml` 格式，為每個階段建立真實的 YAML 規格檔。

### 2. 設置行為憲法
- 讀取 `.agents/templates/AGENT_PROTOCOL.md` 的完整內容，明確替換其中的變數 `{{AGENT_NAME}}` 與 `{{BASE_BRANCH}}`。
- 將替換後的內容另存為 `.{{AGENT_NAME}}/AGENT_PROTOCOL.md`。

### 3. 設置 CI/CD 自動裁判所
- 讀取 `.agents/templates/auto-merge.yml`，明確替換其中的變數 `{{AGENT_NAME}}`, `{{BASE_BRANCH}}`, 與 `{{BOT_USERNAME}}`。
- 將替換後的內容另存為 `.github/workflows/{{AGENT_NAME}}-auto-merge.yml`。

### 4. (條件觸發) API 串接擴充套件
- 所有的系統擴充套件皆存放於 `.agents/extensions/`。
- 若使用者開場選擇了 `jules` 作為 Worker，此專案已內建 `.agents/extensions/jules-api.py`，請直接告知人類已就緒。
- 若人類選擇了非內建支援的選項，則無須建立 API，並告知人類：「已為您產出詳盡框架，執行機制請參考官方文件自行掛載」。

> 🎉 **恭喜 (Finish)**：建廠完成！請向使用者回報專案已可開始運作，並引導他們啟動對應工人的流程！
