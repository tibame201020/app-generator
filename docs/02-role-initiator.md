# 02 如何與架構師溝通 (The Initiator Role)

在無人值守軟體工廠中，**Initiator 架構師**是您唯一需要使用自然語言與之交談的對象。他代表了工廠的「大腦」。

## 啟動架構師 (The Summoning)

架構師的大腦由三個緊密相連的檔案組成，位於 `.agents/skills/initiator/`。
但是，您不需要看這三個檔案，您只需要把主樞紐 `index.md` 丟給您熟悉的 LLM (如 Cursor 的 Composer 或是 Claude Code) 並說出起手式：

> 👉 **「請讀取 `.agents/skills/initiator/index.md`，並開始建廠。」**

## 架構師的三階段供詞流程

這個架構師被施加了「絕對不信任人類」以及「極度悲觀防禦」的 Prompt 約束。他會強迫帶領您走過三個階段：

### Phase 1: 需求釐清與微型任務拆解 (Requirements & Micro-Tasking)
在這個階段，架構師會先探測您的**技術背景**。
- 如果您是 PM，他會用蓋房子的比喻。
- 如果您是工程師，他會直接跟您探討 Cache 機制。
接著，他會強制產出 **ADR (架構決策紀錄)**，確保每一個技術選型都不是幻覺。最後，他會將您的需求切碎成極細顆粒度的 Task。

### Phase 2: 極限沙盤推演 (Mental Simulation)
這是這套系統最具價值的地方。在工人動手之前，架構師會在腦內模擬：
*   **斷線模擬**：如果外部 API 斷掉怎麼辦？(逼迫產出 Fallback 機制)
*   **撞車模擬**：如果兩個非同步相依的任務順序錯了會發生什麼事？

### Phase 3: 工廠建廠 (Factory Scaffolding)
在您點頭同意推演結果後，架構師會安靜地從 `.agents/templates/` 抽取核心模板，並將剛才推演出來的變數（您的專案名稱、分支、任務列表）精準地注入，產生出能讓工人理解的機器碼 `tracker.json` 與 `specs/`。

至此，大腦的工作完成。你可以關閉這個對話視窗了。
