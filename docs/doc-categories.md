# 專案文件知識庫 (doc-categories.md)

> **⚠️ 核心修改規範 (Core Contribution Rules)**
> 本專案功能龐大且各模組深度耦合。為了讓 AI 開發助手（Agent）能安全、精確地修改系統，**在變更任何功能前，必須先閱讀對應的模組文件**。
> 
> 1. **導航與定位**：本文件作為 Root 索引（類似 Skills），將指引你前往 `docs/` 目錄下的各個子系統文件。
> 2. **同步更新承諾**：若變更了資料庫（Schema）或前後端（API/Bridge）設計，**必須同步更新對應的文件**。
> 3. **文件補充承諾**：若在實作或修改過程中，發現目前有功能或模組尚未記錄在現有的文檔中，**必須按照當前 `docs/` 的目錄結構與知識庫格式，主動新增對應的說明文件**並更新本索引，確保知識庫不遺漏。

---

## 🧭 模組文件導航 (Navigation)

### 1. 前端 UI 主題與樣式 (UI Theme & Styling)
本專案採用 DaisyUI 配合 Tailwind CSS 進行全站視覺設計，支援多重主題切換。
- [DaisyUI 與 TailwindCSS 開發指引 (`docs/frontend/theme_and_styling.md`)](frontend/theme_and_styling.md)
  - 涵蓋主題一致性 (Semantic Colors)、組件樣式覆寫規範，與 DaisyUI 的配置實作考量。

### 2. 狀態機與 Agent 流程 (Agent State Machine)
- [Agent 狀態機架構指引 (`docs/backend/state_machine.md`)](backend/state_machine.md)
  - 涵蓋 Jules Software Factory 核心的對話節點控制、職責分配（PM, UIUX, SA, PG）與狀態轉移規則。

*(後續開發 Phase 若有新增文件，請在此處補充。)*
