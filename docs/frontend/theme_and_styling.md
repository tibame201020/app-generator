# DaisyUI 與 TailwindCSS 開發指引

本文件說明 Jules Software Factory 前端專案的 UI 主題與樣式開發規範。

## 1. 主題設定 (Theming)

本專案使用 DaisyUI 作為 UI 元件庫，並搭配 Tailwind CSS 進行排版與樣式微調。

### 支援的主題
目前專案在 `tailwind.config.ts` 中配置了以下 DaisyUI 主題：
- `light` (預設淺色模式)
- `dark` (深色模式)

### 切換主題
DaisyUI 支援透過 `data-theme` 屬性切換主題。
```html
<html data-theme="dark">
```
開發者可透過 React Context 或 Hook 動態修改 `document.documentElement` 的屬性來實作主題切換功能。

## 2. 共用元件引用規範

為了保持 UI 一致性，請遵循以下原則：

### 使用 DaisyUI 元件類別
優先使用 DaisyUI 提供的語意化類別 (Semantic Classes)，避免手寫大量 Tailwind Utility Classes。

**正確範例 (Good):**
```tsx
<button className="btn btn-primary">Submit</button>
<div className="card w-96 bg-base-100 shadow-xl">...</div>
```

**錯誤範例 (Bad):**
```tsx
<button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">Submit</button>
```

### 語意化顏色 (Semantic Colors)
使用 DaisyUI 定義的語意化顏色變數，而非硬編碼的顏色值。
- `primary`: 主要操作 (Primary Action)
- `secondary`: 次要操作 (Secondary Action)
- `accent`: 強調色 (Accent)
- `neutral`: 中性色 (Neutral)
- `base-100`, `base-200`, `base-300`: 背景色層級
- `info`, `success`, `warning`, `error`: 狀態色

## 3. 版面配置 (Layout)

系統採用標準的後台管理介面佈局，包含：
- **Navbar**: 頂部導航列，包含 Logo、主題切換與使用者選單。
- **Sidebar**: 左側功能選單，依據使用者角色顯示不同功能。
- **Main Content**: 右側主要內容區域，透過 `react-router-dom` 渲染對應頁面。

### 常用 Layout Utility
- `min-h-screen`: 確保容器至少佔滿整個螢幕高度。
- `flex`, `grid`: 用於排版佈局。
- `container mx-auto`: 用於置中內容。

## 4. 客製化樣式 (Customization)

若 DaisyUI 的預設樣式無法滿足需求，請優先在 `tailwind.config.ts` 中透過 `theme.extend` 進行擴充，而非在 CSS 檔案中寫死樣式。
特定元件的微調可使用 Tailwind Utility Classes 覆蓋，但應保持簡潔。

---
*文件維護者：Jules (Autonomous Software Factory)*
