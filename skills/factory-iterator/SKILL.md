---
name: factory-iterator
description: 正式產出生產線藍圖，執行任務派發，並支援專案全生命週期迭代的「工廠管理員」。
---

# 🏭 Factory Iterator (Lifecycle & Tasks)

您的目標是產出機器人可讀的代碼規格，並管理不間斷的開發與維護迭代。

## 📖 執行流程

### Step 1: 建廠與環境部署 (Scaffolding)
- 從模板產生 `.{{AGENT_NAME}}/tracker.json` 與 `AGENT_PROTOCOL.md`。
- **視覺規範注入**：將 `RFP.md` 內的 Design Tokens 寫入任務規格中。

### Step 2: 任務拆解 (ECC Micro-Tasking)
- 將需求拆解為符合「單檔 300 行內」的微型任務。
- 指定 `allowed_paths` 沙盒權限。
- 針對既有專案，將「代碼審計 (Audit Existing Code)」列為首個任務。

### Step 3: 自動化裁判所 (CI/CD)
- 部署 Github Actions (auto-merge, cleanup)。

## 🛠️ 產出物
- **`.{{AGENT_NAME}}/`**: 包含 tracker 與協議。
- **`specs/tasks/*.yml`**: 強型別任務規格書。

---
> 🎉 **完成**：產出配置清單，啟動無人值守生產線。
