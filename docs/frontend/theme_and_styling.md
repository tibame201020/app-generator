# DaisyUI 與 TailwindCSS 開發指引

本專案採用 DaisyUI 作為 UI 元件庫，搭配 Tailwind CSS 進行樣式客製化。

## 主題設定 (Theming)

本專案配置了 `light` 與 `dark` 兩種主題，預設為 `light`。
主題切換功能可透過修改 HTML 根元素的 `data-theme` 屬性來達成。

### 顏色變數

DaisyUI 使用語意化顏色變數 (Semantic Colors)，例如：

- `primary`: 主要操作按鈕顏色
- `secondary`: 次要操作顏色
- `accent`: 強調色
- `neutral`: 中性色 (背景、邊框等)
- `base-100`: 頁面背景色
- `info`, `success`, `warning`, `error`: 狀態顏色

若需客製化顏色，請在 `tailwind.config.ts` 中的 `daisyui.themes` 區塊進行設定。

## 共用元件引用規範

請優先使用 DaisyUI 提供的 CSS Component class，避免過度使用 Tailwind Utility class 堆疊。

### 按鈕 (Button)

```tsx
<button className="btn btn-primary">Primary Button</button>
<button className="btn btn-secondary">Secondary Button</button>
<button className="btn btn-ghost">Ghost Button</button>
```

### 卡片 (Card)

```tsx
<div className="card w-96 bg-base-100 shadow-xl">
  <div className="card-body">
    <h2 className="card-title">Card Title</h2>
    <p>If a dog chews shoes whose shoes does he choose?</p>
    <div className="card-actions justify-end">
      <button className="btn btn-primary">Buy Now</button>
    </div>
  </div>
</div>
```

## 版面配置 (Layout)

系統主要 Layout 包含：
- **Navbar**: 頂部導航列
- **Sidebar**: 側邊選單 (響應式)
- **Main Content**: 主要內容區域

詳細 Layout 實作將在後續 Phase 進行開發。
