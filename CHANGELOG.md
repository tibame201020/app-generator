# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
- **Initiator 雙腦架構重構**：將架構決策 (Architect) 與任務規劃 (Planner) 物理分離。
- **Claude Agent Skills 規範化**：遷移至官方 `skills/` 巢狀包結構，支援漸進式揭露。
- **Factory 2.0 機械約束**：引入 `allowed_paths` 路徑審計機制，防止 AI 程式碼漂移。
- **CI 安全強化**：`auto-merge.yml` 升級為 `PAT_TOKEN` 並加入初期 Scaffolding Guard。
- **冷啟動自動化**：Initiator 現在會自動初始化 `docs/` 索引與占位文件。
- **環境自愈**：建立 `docs/ENVIRONMENT_FIX.md` 提供常見錯誤修復指引。

## [0.1.0] - 2026-02-22
- Initiated project boilerplate and AI Agent protocols.
...
