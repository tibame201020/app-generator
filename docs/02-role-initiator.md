# 02 如何與架構師溝通 (The Initiator Role)

在無人值守軟體工廠中，**Initiator 啟動器**是您唯一需要使用自然語言與之交談的對象。他代表了工廠的「大腦」。

## 啟動架構師 (The Summoning)

啟動器的大腦由四個子模組組成，封裝於 `skills/factory-initiator/` 官方技能包中。

> 👉 **「請讀取 `skills/factory-initiator/SKILL.md`，並開始建廠。」**

這個啟動器採用 **雙腦架構 (Dual-Brain)**，將過程拆解為以下四個關鍵步驟：

### Step 1: Architect 高階架構決策
產出 **ADR (架構決策紀錄)** 與 **反模式禁令**。確認技術棧與安全性基調。

### Step 2: Planner 微型任務規劃 (ECC Standard)
將願景拆解為極細顆粒度的 Task。
- **權限沙盒**：為每個任務指定 `allowed_paths`。
- **Success Criteria**：定義量化驗證指標。

### Step 3: Mental Simulation 極限沙盤推演
在工人動手之前，模擬 AI 工人執行過程：
- **邊界模擬**：預判環境配置缺失或 API 衝突。

### Step 4: Factory Scaffolding 工廠建廠
正式將範本注入專案。
- **冷啟動處理**：自動初始化 `docs/doc-categories.md`。
- **安全部署**：部署 `auto-merge.yml` (使用 PAT_TOKEN)。

---

至此，大腦的工作完成。您可以直接按照 `tracker.json` 的規劃啟動工人 Agent 了。
