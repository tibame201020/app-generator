# 04 擴充與外掛指南 (Extensions API)

無人值守軟體工廠框架的設計哲學是 **「插頭與插座」**。
架構師 (Initiator) 只負責把插座 (`tracker.json`) 做好，至於是哪一個廠牌的吸塵器 (Worker Agent) 插進來運作，框架並不在意。

## 解耦的藝術：`.agents/extensions/`

如果您觀察本框架的原始碼，您會發現有一塊名為 `.agents/extensions/` 的資料夾。
這是用來存放「呼叫外部 AI Agent API (或 CLI)」的膠水腳本用的。

### 內建的 Jules API 範例

框架中預設提供了一支 `jules-api.py`，作為參考實作（Reference Implementation）。
當您在使用 Initiator 選擇 `jules` 作為您的工人時，Initiator 就會去讀取這支腳本，並將其放入 CI/CD 或是對應的專案目錄中，讓系統知道「如何把 Jules 這個特定的機器人叫醒」。

### 如何撰寫您自己的 Worker Extension？

假設您今天擁有一個自己的內部 LLM 工具叫做 `CorpBot`，您可以輕鬆地把它變成這個工廠的工人：

1. **建立腳本**：在 `.agents/extensions/` 中新增一支腳本 `corpbot-trigger.js` / `.py` / `.sh`。
2. **實作介面邏輯**：這支腳本只需要做一件事：**找到可以觸發 `CorpBot` 啟動的 API 端點或 CLI 指令**。
3. **丟入環境**：您甚至不需要修改 Initiator 架構師的大腦！當 Initiator 在執行 Phase 3 (Factory Scaffolding) 時，只要您跟架構師說：「我的工人叫做 CorpBot，請你把 `.agents/extensions/corpbot-trigger.sh` 設定到我們的主流程中。」架構師就會完美地把該腳本組裝進您的 CI 或本地排程器裡。

這就是**極致解耦 (Plug and Play)** 帶來的強大擴充性！這個框架可以適應未來五年內任何新出廠的強大 Developer Agents。
