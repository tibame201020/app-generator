---
name: Initiator - Step 3 (Factory Scaffolding)
description: 將經過推演與確認的任務清單，實體化為無人值守軟體工廠的四大核心藍圖。
---

# 🏭 Initiator 階段三：產出四大藍圖 (Scaffolding the Factory)

推演無誤且人類同意後，請使用本手冊最下方的【🏗️ 核心架構範本】，為專案生成完整的「工廠啟動包」。

## 🛠️ 執行指南

在建置工廠前，**您必須先向使用者確認 (或從對話上下文推斷) 以下環境變數**：
- `{{AGENT_NAME}}`：該工廠將配置的 AI 工人名稱 (例如 `jules`, `dev-bot` 等)。
- `{{BASE_BRANCH}}`：專案的主要開發分支 (例如 `feature/ai-factory`, `main` 等)。
- `{{BOT_USERNAME}}`：AI 機器人在 Version Control 平台上的帳號名稱 (預設可填 `your-bot-username`)。

確認完畢後，請為專案生成完整的「工廠啟動包」：

1. **`.{{AGENT_NAME}}/tracker.json` (狀態機)**：參考下方的 `tracker.json` 範本格式，將推演出的所有依賴任務寫入，預設狀態設為 `pending`。
2. **`specs/phase_X.yml` (規格防爆網)**：建立 `specs/` 目錄，並參考下方的 `template.yml` 格式，為每個階段建立真實的 YAML 規格。
3. **`.{{AGENT_NAME}}/AGENT_PROTOCOL.md` (行為憲法)**：將下方的 `AGENT_PROTOCOL.md` 內容建立至目標路徑，並將其中的 `{{...}}` 變數替換為真實值。
4. **`.github/workflows/{{AGENT_NAME}}-auto-merge.yml` (CI 裁判所)**：將下方的 YAML 內容建立至目標路徑，並替換變數。
5. **(條件觸發) API 串接擴充套件**：若使用者剛剛選擇了 `jules` 作為 Worker，請將最下方的 `jules-api.py` Python 腳本完整建立於 `.agents/extensions/jules-api.py` 中，藉此啟用它的完全自動化。若非支援選項，則無須建立此檔，並告知人類：「已為您產出詳盡框架，執行機制請由外部手動觸發」。

---

## 🏗️ 核心架構範本 (Core Architecture Templates)
> **非常重要**：在進行建廠時，請直接拷貝以下四個檔案的內容至對於路徑，**不要省略任何一字一句**，因為它們包含了維持工廠運作的關鍵 Git-as-State-Machine 物理法則。

### 1. `tracker.json` 基本骨架參考
```json
{
  "project": "您的專案名稱",
  "current_phase": "Phase 1: Setup",
  "phases": [
    {
      "phase_id": "phase_1",
      "name": "Phase 1: Setup",
      "tasks": [
        // 填入您設計的 tasks
      ]
    }
  ]
}
```

### 2. `specs/*.yml` 格式範本參考
```yaml
id: "phase_1"
name: "Phase 1: Setup"
description: "在此填寫階段描述，例如：建置底層架構與核心模組"
objectives:
  - "建立專案骨架"
modules:
  - id: "1.1"
    name: "專案初始化"
    tasks:
      - id: "task_1_1_1" # 必須與 tracker.json 中的 ID 完全一致
        title: "初始化專案"
        objective: "客觀具體目標描述"
        acceptance_criteria:
          - "建立 pom.xml 或 package.json"
          - "[禁令] 絕對不可以做ＯＯＯ"
          - "[Fallback] API Key 必須設定預設值"
```

### 3. `.{{AGENT_NAME}}/AGENT_PROTOCOL.md` 必須內文 (請完整複製並替換變數)
```markdown
# {{AGENT_NAME}} Agent Execution Protocol
> 每次 Schedule 觸發後，{{AGENT_NAME}} 必須依序執行以下步驟，不得跳過。

## Step 1: Read State
- 讀取 `.{{AGENT_NAME}}/tracker.json`
- 找出第一個狀態為 `pending` 且 `depends_on` 中所有 task 均為 `completed` 的 task。
- 若找不到符合條件的 task，輸出 log「No actionable task found. Halting.」並終止。

## Step 2: Acquire Context
- 將該 task 的 `spec_ref` 對應的 spec 文件 (`.yml` 格式) 完整讀取。
- 讀取所有 `.{{AGENT_NAME}}/skills/*.md` 技術規範。
- **重要：讀取 `docs/doc-categories.md` 知識庫索引**，並根據即將修改的模組，導航至 `docs/` 對應的子文件閱讀。
- 將該 task 的 `status` 更新為 `in_progress` 並 commit。

## Step 3: Implement
- 依照 spec 實作功能，嚴格遵守 skills 文件中的程式碼風格。
- 實作必須包含：功能程式碼 + 對應的單元測試／整合測試。
- 若有架構或 Schema 變更，必須同步更新 `docs/` 內的對應文件與 `CHANGELOG.md`。

## Step 4: Self-Healing & Autonomy (自我修復與自治)
- 雖然需嚴格遵守 Spec，但身為高階 Agent，**您被授權進行邏輯上的自我修復與環境適應**。
- 若遇到未列於 Spec 但為達成功能**絕對必要**的缺失（例如：框架衝突、缺少依賴套件、環境變數遺漏、或是前置任務邏輯導致編譯失敗）。
- **授權行為**：您可自行加入必要的配置、微調架構或修正先前的錯誤，並將此「自主修正 (Self-Healing)」的紀錄寫入 `CHANGELOG.md` 及 PR 描述中。
- 目標是：**在不偏離核心功能的目標下，確保程式碼能 100% 成功執行與編譯。**

## Step 5: Validate
- 執行所有測試（後端 `mvn test`，前端 TypeScript 檢查與 Lint），確認全數通過。
- 對照 spec 的 Acceptance Criteria 逐條自我檢查。
- 對照相關 skill 文件末尾的 PR Checklist 逐條確認。
- 若任何一條未通過，回到 Step 3 或 Step 4 修正，不得帶著失敗的測試提 PR。

## Step 6: Finalize Status & Submit PR
- 專案的主開發分支為 `{{BASE_BRANCH}}`。
- {{AGENT_NAME}} 每次執行任務時，必須從 `{{BASE_BRANCH}}` 切出新分支：`{{AGENT_NAME}}/task-{task_id}`。
- **重要狀態轉移**：在您確認所有測試通過、程式碼完成後，**您必須親自將 `.{{AGENT_NAME}}/tracker.json` 中該任務的 status 改為 `completed` 並 commit**，這代表您對本次任務的品質背書。
- 提交 PR 時，目標分支 (Base Branch) 必須設定為 `{{BASE_BRANCH}}`。
- PR Title 格式：`[{{AGENT_NAME}}] {task_title}`
- PR Description 必須包含：
  - 對應 Task ID。
  - 已完成的 Acceptance Criteria 列表（逐條勾選）。
  - 測試覆蓋摘要。
  - 所影響的文件或 `CHANGELOG.md` 變更說明。
  - **必須在結尾標註 `[auto-merge]` 標籤**，以便觸發 GitHub Actions 的自動合併機制。

## Step 7: Wait for CI/CD Auto-Merge (Git as State Machine)
- 您提交的 PR 在通過 GitHub Actions 的自動測試後，自動合併機器人 (如 enable-pull-request-automerge) 會自動將其 Squash Merge 至 `{{BASE_BRANCH}}` 分支。
- **因為您已經在 PR 中將 tracker 改成了 completed**，只要 PR 測試通過且順利被 Merge，主分支的 tracker 就會自然成為 completed 狀態。
- 若 PR 測試失敗遭到 CI 阻擋，該 PR 就不會 Merge，主分支的狀態仍會保持 pending/in_progress。下次您醒來時，就會發現任務依舊尚未完成，從而繼續修復它。
- **{{AGENT_NAME}} 的唯一責任就是在 Step 6 提好包含 completed 狀態的乾淨 PR，接著就可以直接離線**，直到下一次 Schedule 被系統喚醒。
```

### 4. `.github/workflows/{{AGENT_NAME}}-auto-merge.yml` 必須內文 (請完整複製並替換變數)
```yaml
name: {{AGENT_NAME}} Auto-Merge

on:
  pull_request:
    types: [opened, synchronize, reopened, ready_for_review, closed]
    branches:
      - '{{BASE_BRANCH}}'

permissions:
  contents: write
  pull-requests: write

jobs:
  auto-merge:
    runs-on: ubuntu-latest
    if: |
      github.event.action != 'closed' &&
      (github.event.pull_request.user.login == '{{BOT_USERNAME}}' || 
      contains(github.event.pull_request.body, '[auto-merge]'))
    
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      # CI: 動態偵測防護機制
      # 當 Phase 1 建出 backend 與 frontend 後，這些測試將自動硬性生效
      - name: Backend Tests (Maven)
        run: |
          if [ -f "./pom.xml" ]; then
            ./mvnw clean test
          elif [ -f "backend/pom.xml" ]; then
            cd backend && ./mvnw clean test
          else
            echo "Backend not initialized yet. Skipping tests."
          fi

      - name: Frontend Checks (NPM)
        run: |
          if [ -f "./package.json" ]; then
            npm install && npm run build
          elif [ -f "frontend/package.json" ]; then
            cd frontend && npm install && npm run build
          else
            echo "Frontend not initialized yet. Skipping checks."
          fi

      - name: Auto-merge PR
        uses: peter-evans/enable-pull-request-automerge@v3
        with:
          pull-request-number: ${{ github.event.pull_request.number }}
          merge-method: squash
          token: ${{ secrets.GITHUB_TOKEN }}
```

### 5. `.agents/extensions/jules-api.py` (僅限 Jules 使用)
> 如果人類選擇配置 `jules`，請完整建立以下檔案以啟用其自動化 API 控制。

```python
#!/usr/bin/env python3
import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import quote

import requests

API_BASE = "https://jules.googleapis.com/v1alpha"
STATE_PATH = Path(__file__).resolve().parent.parent / "state" / "jules_state.json"


def _api_key(args):
    key = args.api_key or os.getenv("JULES_API_KEY")
    if not key:
        raise SystemExit("Missing API key: set --api-key or JULES_API_KEY")
    return key


def _headers(key):
    return {"x-goog-api-key": key, "Content-Type": "application/json"}


def _get(key, path, params=None):
    r = requests.get(f"{API_BASE}{path}", headers=_headers(key), params=params, timeout=60)
    r.raise_for_status()
    return r.json() if r.text.strip() else {}


def _post(key, path, payload=None):
    r = requests.post(f"{API_BASE}{path}", headers=_headers(key), json=(payload or {}), timeout=60)
    r.raise_for_status()
    return r.json() if r.text.strip() else {}


def _save_state(data):
    STATE_PATH.parent.mkdir(parents=True, exist_ok=True)
    if STATE_PATH.exists():
        try:
            old = json.loads(STATE_PATH.read_text(encoding="utf-8"))
        except Exception:
            old = {}
    else:
        old = {}
    old.update(data)
    STATE_PATH.write_text(json.dumps(old, ensure_ascii=False, indent=2), encoding="utf-8")


def _load_state():
    if not STATE_PATH.exists():
        return {}
    try:
        return json.loads(STATE_PATH.read_text(encoding="utf-8"))
    except Exception:
        return {}


def cmd_list_sources(args):
    key = _api_key(args)
    data = _get(key, "/sources")
    sources = data.get("sources", [])
    if args.filter:
        sources = [s for s in sources if args.filter.lower() in s.get("name", "").lower()]
    print(json.dumps({"sources": sources}, ensure_ascii=False, indent=2))


def _create_session(key, source, prompt, title, branch, automation_mode="AUTO_CREATE_PR"):
    payload = {
        "prompt": prompt,
        "sourceContext": {
            "source": source,
            "githubRepoContext": {"startingBranch": branch},
        },
        "automationMode": automation_mode,
        "title": title,
    }
    return _post(key, "/sessions", payload)


def cmd_trigger(args):
    key = _api_key(args)
    sess = _create_session(key, args.source, args.prompt, args.title, args.branch, args.automation_mode)
    _save_state({"lastSession": sess.get("name"), "source": args.source})
    print(json.dumps(sess, ensure_ascii=False, indent=2))


def _list_sessions(key, page_size=50):
    data = _get(key, "/sessions", {"pageSize": page_size})
    return data.get("sessions", [])


def _session_source_name(s):
    return ((s.get("sourceContext") or {}).get("source") or "")


def _extract_pr_urls(session):
    urls = []
    for out in session.get("outputs", []) or []:
        pr = out.get("pullRequest") or {}
        url = pr.get("url")
        if url:
            urls.append(url)
    return urls


def _latest_for_source(key, source):
    sessions = _list_sessions(key)
    filtered = [s for s in sessions if _session_source_name(s) == source]
    if not filtered:
        return None
    # API usually returns desc; keep first as latest
    return filtered[0]


def cmd_latest(args):
    key = _api_key(args)
    s = _latest_for_source(key, args.source)
    if not s:
        print(json.dumps({"found": False, "source": args.source}, ensure_ascii=False, indent=2))
        return
    out = {
        "found": True,
        "name": s.get("name"),
        "id": s.get("id"),
        "title": s.get("title"),
        "prompt": s.get("prompt"),
        "prUrls": _extract_pr_urls(s),
        "raw": s,
    }
    print(json.dumps(out, ensure_ascii=False, indent=2))


def _merge_pr(pr_url, method="squash"):
    cmd = ["gh", "pr", "merge", pr_url, f"--{method}", "--delete-branch"]
    r = subprocess.run(cmd, capture_output=True, text=True)
    return {
        "ok": r.returncode == 0,
        "command": " ".join(cmd),
        "stdout": r.stdout.strip(),
        "stderr": r.stderr.strip(),
    }


def cmd_cycle(args):
    key = _api_key(args)
    latest = _latest_for_source(key, args.source)
    status = {
        "source": args.source,
        "latestSession": latest.get("name") if latest else None,
        "merged": [],
        "triggered": None,
        "note": "",
    }

    pr_urls = _extract_pr_urls(latest) if latest else []

    if args.merge == "yes" and pr_urls:
        # Merge at most one PR per cycle (minimal-step policy)
        merge_result = _merge_pr(pr_urls[0], method=args.merge_method)
        status["merged"].append({"pr": pr_urls[0], **merge_result})
    elif args.merge == "yes":
        status["note"] = "No PR found from latest Jules session."

    # Always trigger one new async request when next_prompt provided
    if args.next_prompt:
        title = args.title or "jules-next-step"
        created = _create_session(key, args.source, args.next_prompt, title, args.branch, args.automation_mode)
        status["triggered"] = {"name": created.get("name"), "id": created.get("id"), "title": created.get("title")}
        _save_state({"lastSession": created.get("name"), "source": args.source})

    print(json.dumps(status, ensure_ascii=False, indent=2))


def build_parser():
    p = argparse.ArgumentParser(description="Jules API helper for async trigger/check/merge cycle")
    sub = p.add_subparsers(dest="cmd", required=True)

    ps = sub.add_parser("list-sources")
    ps.add_argument("--api-key")
    ps.add_argument("--filter")
    ps.set_defaults(func=cmd_list_sources)

    pt = sub.add_parser("trigger")
    pt.add_argument("--api-key")
    pt.add_argument("--source", required=True)
    pt.add_argument("--prompt", required=True)
    pt.add_argument("--title", default="jules-task")
    pt.add_argument("--branch", default="main")
    pt.add_argument("--automation-mode", default="AUTO_CREATE_PR")
    pt.set_defaults(func=cmd_trigger)

    pl = sub.add_parser("latest")
    pl.add_argument("--api-key")
    pl.add_argument("--source", required=True)
    pl.set_defaults(func=cmd_latest)

    pc = sub.add_parser("cycle")
    pc.add_argument("--api-key")
    pc.add_argument("--repo", help="owner/repo (for bookkeeping)")
    pc.add_argument("--source", required=True)
    pc.add_argument("--branch", default="main")
    pc.add_argument("--next-prompt")
    pc.add_argument("--title")
    pc.add_argument("--automation-mode", default="AUTO_CREATE_PR")
    pc.add_argument("--merge", choices=["yes", "no"], default="yes")
    pc.add_argument("--merge-method", choices=["squash", "merge", "rebase"], default="squash")
    pc.set_defaults(func=cmd_cycle)

    return p


def main():
    parser = build_parser()
    args = parser.parse_args()
    try:
        args.func(args)
    except requests.HTTPError as e:
        body = e.response.text if e.response is not None else str(e)
        print(json.dumps({"error": "http_error", "status": e.response.status_code if e.response is not None else None, "body": body}, ensure_ascii=False, indent=2))
        sys.exit(1)


if __name__ == "__main__":
    main()
```
