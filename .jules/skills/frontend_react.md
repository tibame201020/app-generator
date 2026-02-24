# Frontend React Skills

## Tech Stack
- Vite + React (TypeScript)
- Tailwind CSS + DaisyUI
- WebSockets for Real-time communication

## Coding Guidelines
1. **TypeScript Typing**: Avoid `any`. Define proper interfaces for all API payloads and internal state.
2. **DaisyUI & Tailwind**: Utilize DaisyUI components (`btn`, `card`, `alert`) wherever possible instead of custom Tailwind utility strings to maintain consistency.
3. **State Management**: Use React Hooks (`useState`, `useContext`) properly. For complex state machines, abstract into custom hooks.
4. **WebSocket Handling**: Ensure reconnect logic is implemented for WebSocket connections.

## PR Checklist
- [ ] 執行 `tsc --noEmit` 無型別錯誤。
- [ ] 執行 `npm run lint` 無任何警告或錯誤。
- [ ] UI 元件皆使用 DaisyUI 類別實作，未使用強制覆蓋的重要樣式（除非必要且有註解）。
- [ ] 已同步撰寫與更新 `CHANGELOG.md` 中提及對應的 Task ID 與前端變更內容。
