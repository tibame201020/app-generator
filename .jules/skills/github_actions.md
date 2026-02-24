# CI/CD & GitHub Actions Skills

## Tech Stack
- GitHub Actions

## Coding Guidelines
1. **Branching**: All iterative development happens on the `feature/jules-factory` branch.
2. **Pull Requests**: Every PR must target `feature/jules-factory` and must be capable of passing all required status checks.
3. **Auto-Merge Tag**: The PR body MUST contain the `[auto-merge]` tag if the PR is expected to be merged automatically without human intervention.
4. **Failing Checks**: If GitHub Actions (e.g., Maven test, TSC lint) fail, the PR should not be auto-merged. Jules must read the failing logs and open a new PR or push a fix commit.

## PR Checklist
- [ ] PR 目標分支已正確設定為 `feature/jules-factory`。
- [ ] PR 描述內已包含 `[auto-merge]` 觸發標籤。
- [ ] 本次變更不會破壞既有的 GitHub Action YAML 結構。
