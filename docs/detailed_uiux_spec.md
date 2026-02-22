# 詳細規格書：UI/UX 介面與互動設計

## 1. 介面佈局 (Layout Grid)
採用 **VS Code 經典三欄式佈局**，並針對 Agent 協作特質進行優化。

### 1.1 全域佈局結構 (Main Shell)
- **Top Bar (48px)**: 
    - 左側：專案切換下拉選單、存檔狀態燈。
    - 中間：當前處於活躍狀態的 Agent 提示（如：`PG Agent is coding...`）。
    - 右側：`Run/Debug` 按鈕、`Deploy` 按鈕、使用者個人資料。
- **Activity Bar (48px, 側邊最左)**: 
    - 圖示連結：檔案管理、擴充元件、流程畫布切換、系統設定。
- **Side Bar (260px, 可伸縮)**: 
    - 內容隨 Activity Bar 切換。預設為 `File Explorer`。
- **Main Workspace (Flexible)**: 
    - 中心主體，採用 Tab 系統。
- **Right Panel (320px, 可伸縮)**: 
    - **Agent Chat 視窗**：核心對話入口。
- **Bottom Panel (可伸縮, 預設 200px)**: 
    - 包含：`Terminal` (xterm.js)、`Output` (系統日誌)、`Debug Console`。

---

## 2. 核心元件設計 (Component Details)

### 2.1 視覺化畫布 (Workflow Canvas)
基於 **React Flow** 實作，定義以下節點類型：
- **Agent Node**:
    - 圓形或方塊，顯示 Avatar、角色名稱（PM/SA/PG）。
    - 狀態環：藍色旋轉（處理中）、綠色（完成）、紅色（報錯）。
- **Artifact Node**:
    - 顯示生成的產出物名稱（如：`user-service.java`）。
    - 點擊後主編輯區 Tab 切換至該代碼。
- **Edge Animation**:
    - 數據流向時，連線呈現「脈衝點」移動動畫。

### 2.2 代碼編輯器 (Monaco Editor)
- **主題**: 預設 Dark (VS Code Dark+ 風格)。
- **冷熱標記**: 當 AI 正在修改某行代碼時，該行側邊出現動態標記，不可手動輸入。

---

## 3. 狀態機與交互流程 (State Transitions)

### 3.1 既有專案匯入流 (Import Sequence)
1.  **Drop Zone**: 使用者拖入 Zip。
2.  **Overlay**: 畫布層出現半透明遮罩，顯示「解壓與結構掃描中...」。
3.  **Skeleton Pop**: 畫布中心爆發式長出節點，代表偵測到的 Class 與 API。
4.  **Async Progress**: 側邊欄顯示總進度條，隨著 LLM 摘要完成，畫布節點逐漸由置灰色變為有色。

### 3.2 預覽啟動流 (Run Sequence)
1.  點擊 **Run**。
2.  底部 **Terminal** 自動彈出。
3.  顯示序列：`[System] Creating Sandbox...` -> `[Build] Maven Compiling...` -> `[Server] Spring Boot Starting...`。
4.  完成時，畫布右上方彈出 **Preview Float Window** 或跳轉 Preview Tab。

---

## 4. 配色方案 (Color Palette)
- **Primary**: `#007ACC` (VS Blue)
- **Success**: `#28A745` (Done)
- **Warning**: `#FFC107` (Planning)
- **Danger**: `#DC3545` (Error)
- **Background**: `#1E1E1E` (Editor), `#252526` (Sidebar)
